package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.BookingRequest;
import ba.unsa.etf.nbp.travel.dto.response.BookingResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.security.AuthContext;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Role({"USER"})
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        var userId = AuthContext.get().userId();
        var response = bookingService.create(userId, request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
    @Role({"ADMIN", "AGENT"})
    public ResponseEntity<PageResponse<BookingResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.findAll(page, size));
    }

    @GetMapping("/my")
    @Role({"USER"})
    public ResponseEntity<PageResponse<BookingResponse>> findMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var userId = AuthContext.get().userId();
        return ResponseEntity.ok(bookingService.findByUserId(userId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @PutMapping("/{id}/confirm")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<BookingResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id) {
        var userId = AuthContext.get().userId();
        return ResponseEntity.ok(bookingService.cancel(id, userId));
    }
}
