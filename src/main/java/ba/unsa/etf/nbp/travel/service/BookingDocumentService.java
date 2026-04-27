package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.exception.ForbiddenException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import ba.unsa.etf.nbp.travel.security.AuthContext;
import ba.unsa.etf.nbp.travel.storage.DocumentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class BookingDocumentService {

    private final BookingRepository bookingRepository;
    private final DocumentStorage documentStorage;

    public enum DocumentType {
        RECEIPT("receipt.pdf"),
        VOUCHER("voucher.pdf");

        private final String filename;

        DocumentType(String filename) {
            this.filename = filename;
        }

        public String filename() {
            return filename;
        }
    }

    public void uploadReceipt(Long bookingId, InputStream data, long length, String contentType) {
        var user = AuthContext.get();
        assertUserOwnsBooking(bookingId, user.userId());
        documentStorage.upload(blobName(bookingId, DocumentType.RECEIPT), data, length, contentType);
    }

    public void uploadVoucher(Long bookingId, InputStream data, long length, String contentType) {
        ensureBookingExists(bookingId);
        documentStorage.upload(blobName(bookingId, DocumentType.VOUCHER), data, length, contentType);
    }

    public DocumentStorage.StoredDocument downloadReceipt(Long bookingId) {
        var user = AuthContext.get();
        if ("USER".equalsIgnoreCase(user.role())) {
            assertUserOwnsBooking(bookingId, user.userId());
        } else {
            ensureBookingExists(bookingId);
        }
        return safeDownload(bookingId, DocumentType.RECEIPT);
    }

    public DocumentStorage.StoredDocument downloadVoucher(Long bookingId) {
        var user = AuthContext.get();
        if ("USER".equalsIgnoreCase(user.role())) {
            assertUserOwnsBooking(bookingId, user.userId());
        } else {
            ensureBookingExists(bookingId);
        }
        return safeDownload(bookingId, DocumentType.VOUCHER);
    }

    private DocumentStorage.StoredDocument safeDownload(Long bookingId, DocumentType type) {
        try {
            return documentStorage.download(blobName(bookingId, type));
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException(type.name().toLowerCase() + " document for booking", bookingId);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ResourceNotFoundException(type.name().toLowerCase() + " document for booking", bookingId);
            }
            throw e;
        }
    }

    private void ensureBookingExists(Long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void assertUserOwnsBooking(Long bookingId, Long userId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this booking");
        }
    }

    private static String blobName(Long bookingId, DocumentType type) {
        return "bookings/" + bookingId + "/" + type.filename();
    }
}
