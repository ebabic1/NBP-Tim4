package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.PaymentRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.PaymentResponse;
import ba.unsa.etf.nbp.travel.security.AuthContext;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.BookingService;
import ba.unsa.etf.nbp.travel.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    @PostMapping("/bookings/{bookingId}/payment")
    @Role({"USER"})
    public ResponseEntity<PaymentResponse> create(@PathVariable Long bookingId,
                                                   @Valid @RequestBody PaymentRequest request) {
        var userId = AuthContext.get().userId();
        var response = paymentService.create(bookingId, request, userId);
        bookingService.confirm(bookingId);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<PaymentResponse> findByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.findByBookingId(bookingId));
    }

    @GetMapping("/payments/my")
    @Role({"USER"})
    public ResponseEntity<PageResponse<PaymentResponse>> findMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var userId = AuthContext.get().userId();
        return ResponseEntity.ok(paymentService.findMyPayments(userId, page, size));
    }

    @GetMapping("/payments")
    @Role({"ADMIN", "AGENT"})
    public ResponseEntity<PageResponse<PaymentResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(paymentService.findAll(page, size));
    }
}
