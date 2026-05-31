package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.response.ImportResult;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class XmlImportService {

    private final BookingRepository bookingRepository;

    @Transactional
    public ImportResult importXml(String xml) {
        return bookingRepository.importBookingsXml(xml);
    }
}