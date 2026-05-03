package ba.unsa.etf.nbp.travel.storage;

import ba.unsa.etf.nbp.travel.model.entity.BookingDocumentEntity;
import ba.unsa.etf.nbp.travel.repository.BookingDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class DbDocumentStorage implements DocumentStorage {

    private final BookingDocumentRepository bookingDocumentRepository;

    @Override
    public void upload(String blobName, InputStream data, long length, String contentType) {
        ParsedKey key = ParsedKey.parse(blobName);
        byte[] bytes = readAllBytes(data);

        bookingDocumentRepository.upsert(BookingDocumentEntity.builder()
                .bookingId(key.bookingId())
                .docType(key.docType())
                .filename(key.filename())
                .contentType(contentType)
                .content(bytes)
                .sizeBytes((long) bytes.length)
                .build());
    }

    @Override
    public StoredDocument download(String blobName) {
        ParsedKey key = ParsedKey.parse(blobName);
        var e = bookingDocumentRepository.findByBookingIdAndType(key.bookingId(), key.docType())
                .orElse(null);
        if (e == null || e.getContent() == null) {
            return null;
        }
        return new StoredDocument(new ByteArrayInputStream(e.getContent()), e.getContent().length, e.getContentType());
    }

    private static byte[] readAllBytes(InputStream in) {
        try (in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            in.transferTo(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload stream", e);
        }
    }

    private record ParsedKey(Long bookingId, String docType, String filename) {

        static ParsedKey parse(String blobName) {
            // Expected: bookings/{bookingId}/{filename}, where filename is receipt.pdf or voucher.pdf
            if (blobName == null) {
                throw new IllegalArgumentException("blobName is required");
            }
            var parts = blobName.split("/");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid blobName: " + blobName);
            }
            Long bookingId = Long.valueOf(parts[1]);
            String filename = parts[2];
            String docType = filename.startsWith("receipt") ? "RECEIPT" : "VOUCHER";
            return new ParsedKey(bookingId, docType, filename);
        }
    }
}

