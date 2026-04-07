package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.ReviewRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.ReviewResponse;
import ba.unsa.etf.nbp.travel.security.AuthContext;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/bookings/{bookingId}/review")
    @Role({"USER"})
    public ResponseEntity<ReviewResponse> create(
            @PathVariable Long bookingId,
            @Valid @RequestBody ReviewRequest request) {
        var userId = AuthContext.get().userId();
        var response = reviewService.create(bookingId, userId, request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping("/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.findAll(page, size));
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping("/travel-packages/{travelPackageId}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> findByTravelPackageId(
            @PathVariable Long travelPackageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.findByTravelPackageId(travelPackageId, page, size));
    }

    @GetMapping("/accommodations/{accommodationId}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> findByAccommodationId(
            @PathVariable Long accommodationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.findByAccommodationId(accommodationId, page, size));
    }

    @DeleteMapping("/reviews/{id}")
    @Role({"ADMIN", "USER"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var currentUser = AuthContext.get();
        var currentUserId = "ADMIN".equals(currentUser.role()) ? null : currentUser.userId();
        reviewService.delete(id, currentUserId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
