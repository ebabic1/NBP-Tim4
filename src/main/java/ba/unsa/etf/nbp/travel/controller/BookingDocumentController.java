package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.exception.BadRequestException;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.BookingDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingDocumentController {

    private final BookingDocumentService bookingDocumentService;

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Role({"USER"})
    public ResponseEntity<Void> uploadReceipt(@PathVariable("id") Long bookingId,
                                              @RequestPart("file") MultipartFile file) throws IOException {
        validatePdf(file);
        bookingDocumentService.uploadReceipt(bookingId, file.getInputStream(), file.getSize(), MediaType.APPLICATION_PDF_VALUE);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping(value = "/{id}/voucher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Role({"ADMIN", "AGENT"})
    public ResponseEntity<Void> uploadVoucher(@PathVariable("id") Long bookingId,
                                              @RequestPart("file") MultipartFile file) throws IOException {
        validatePdf(file);
        bookingDocumentService.uploadVoucher(bookingId, file.getInputStream(), file.getSize(), MediaType.APPLICATION_PDF_VALUE);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/{id}/receipt")
    @Role({"USER", "ADMIN", "AGENT"})
    public ResponseEntity<InputStreamResource> downloadReceipt(@PathVariable("id") Long bookingId) {
        var doc = bookingDocumentService.downloadReceipt(bookingId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("booking-" + bookingId + "-receipt.pdf"))
                .contentType(MediaType.parseMediaType(doc.contentType() != null ? doc.contentType() : MediaType.APPLICATION_PDF_VALUE))
                .contentLength(doc.length())
                .body(new InputStreamResource(doc.data()));
    }

    @GetMapping("/{id}/voucher")
    @Role({"USER", "ADMIN", "AGENT"})
    public ResponseEntity<InputStreamResource> downloadVoucher(@PathVariable("id") Long bookingId) {
        var doc = bookingDocumentService.downloadVoucher(bookingId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("booking-" + bookingId + "-voucher.pdf"))
                .contentType(MediaType.parseMediaType(doc.contentType() != null ? doc.contentType() : MediaType.APPLICATION_PDF_VALUE))
                .contentLength(doc.length())
                .body(new InputStreamResource(doc.data()));
    }

    private static void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        var name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only .pdf files are allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("Max file size is 10MB");
        }
    }

    private static String contentDisposition(String filename) {
        return "attachment; filename=\"" + filename.replace("\"", "") + "\"";
    }
}

