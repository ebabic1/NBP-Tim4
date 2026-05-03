package ba.unsa.etf.nbp.travel.model.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingDocumentEntity {
    private Long id;
    private Long bookingId;
    private String docType;
    private String filename;
    private String contentType;
    private byte[] content;
    private Long sizeBytes;
}

