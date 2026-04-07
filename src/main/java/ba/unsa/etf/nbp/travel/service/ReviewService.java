package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.ReviewRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.ReviewResponse;
import ba.unsa.etf.nbp.travel.exception.BadRequestException;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ForbiddenException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.ReviewEntity;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import ba.unsa.etf.nbp.travel.repository.ReviewRepository;
import ba.unsa.etf.nbp.travel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static ba.unsa.etf.nbp.travel.mapper.ReviewMapper.toResponse;
import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.CONFIRMED;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewResponse create(Long bookingId, Long userId, ReviewRequest request) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("Booking does not belong to the current user");
        }

        if (!CONFIRMED.name().equals(booking.getStatus())) {
            throw new BadRequestException("Can only review confirmed bookings");
        }

        if (reviewRepository.existsByUserIdAndBookingId(userId, bookingId)) {
            throw new ConflictException("Already reviewed");
        }

        var entity = ReviewEntity.builder()
                .userId(userId)
                .bookingId(bookingId)
                .rating(request.rating())
                .comment(request.comment())
                .reviewDate(LocalDate.now())
                .build();

        var id = reviewRepository.save(entity);
        entity.setId(id);

        var username = getUsername(userId);
        return toResponse(entity, username);
    }

    public PageResponse<ReviewResponse> findAll(int page, int size) {
        var reviews = reviewRepository.findAll(page, size);
        var total = reviewRepository.count();
        var content = reviews.stream()
                .map(r -> toResponse(r, getUsername(r.getUserId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<ReviewResponse> findByTravelPackageId(Long travelPackageId, int page, int size) {
        var reviews = reviewRepository.findByTravelPackageId(travelPackageId, page, size);
        var total = reviewRepository.countByTravelPackageId(travelPackageId);
        var content = reviews.stream()
                .map(r -> toResponse(r, getUsername(r.getUserId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<ReviewResponse> findByAccommodationId(Long accommodationId, int page, int size) {
        var reviews = reviewRepository.findByAccommodationId(accommodationId, page, size);
        var total = reviewRepository.countByAccommodationId(accommodationId);
        var content = reviews.stream()
                .map(r -> toResponse(r, getUsername(r.getUserId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public ReviewResponse findById(Long id) {
        var entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        return toResponse(entity, getUsername(entity.getUserId()));
    }

    public void delete(Long id, Long currentUserId) {
        var entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        if (!isNull(currentUserId) && !entity.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);
    }

    private String getUsername(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse(null);
    }
}
