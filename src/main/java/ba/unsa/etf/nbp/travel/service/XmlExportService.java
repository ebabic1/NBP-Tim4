package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.model.entity.BookingEntity;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
public class XmlExportService {

    private final BookingRepository bookingRepository;

    private static final XMLOutputFactory XML_FACTORY = XMLOutputFactory.newInstance();

    public String exportAllBookings() {
        var entities = bookingRepository.findAll();
        var sw = new StringWriter();
        try {
            var writer = XML_FACTORY.createXMLStreamWriter(sw);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("bookings");
            for (var e : entities) {
                writeBooking(writer, e);
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Failed to generate XML", ex);
        }
        return sw.toString();
    }

    private void writeBooking(XMLStreamWriter w, BookingEntity e) throws XMLStreamException {
        w.writeStartElement("booking");
        writeElement(w, "id", e.getId());
        writeElement(w, "userId", e.getUserId());
        writeElement(w, "bookingType", e.getBookingType());
        writeElement(w, "bookingDate", e.getBookingDate());
        writeElement(w, "status", e.getStatus());
        writeElement(w, "totalPrice", e.getTotalPrice());
        writeElement(w, "travelPackageId", e.getTravelPackageId());
        writeElement(w, "accommodationId", e.getAccommodationId());
        writeElement(w, "transportId", e.getTransportId());
        w.writeEndElement();
    }

    private void writeElement(XMLStreamWriter w, String name, Object value) throws XMLStreamException {
        w.writeStartElement(name);
        if (value != null) {
            w.writeCharacters(value.toString());
        }
        w.writeEndElement();
    }
}