package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.PaymentRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.PaymentResponse;
import ba.unsa.etf.nbp.travel.exception.BadRequestException;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ForbiddenException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import ba.unsa.etf.nbp.travel.repository.DiscountRepository;
import ba.unsa.etf.nbp.travel.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static ba.unsa.etf.nbp.travel.mapper.PaymentMapper.toResponse;
import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.CANCELLED;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DiscountRepository discountRepository;

    @Transactional
    public PaymentResponse create(Long bookingId, PaymentRequest request, Long currentUserId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to pay for this booking");
        }

        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            throw new ConflictException("Payment already exists");
        }

        if (CANCELLED.name().equals(booking.getStatus())) {
            throw new BadRequestException("Cannot pay for a cancelled booking");
        }

        Long discountId = null;

        if (nonNull(request.discountCode()) && !request.discountCode().isBlank()) {
            var discount = discountRepository.findByCode(request.discountCode())
                    .orElseThrow(() -> new BadRequestException("Discount code not found: " + request.discountCode()));

            var now = LocalDate.now();
            if (now.isBefore(discount.getValidFrom()) || now.isAfter(discount.getValidTo())) {
                throw new BadRequestException("Discount code expired");
            }

            discountId = discount.getId();
        }

        Long id;
        try {
            id = paymentRepository.createWithPackage(bookingId, request.method(), discountId);
            paymentRepository.completeWithPackage(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Discount code already used");
        }

        var payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        return toResponse(payment);
    }

    public PaymentResponse findById(Long id, Long currentUserId, String currentRole) {
        var entity = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        checkOwnership(entity.getBookingId(), currentUserId, currentRole);
        return toResponse(entity);
    }

    public PaymentResponse findByBookingId(Long bookingId, Long currentUserId, String currentRole) {
        var entity = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for booking", bookingId));
        checkOwnership(bookingId, currentUserId, currentRole);
        return toResponse(entity);
    }

    private void checkOwnership(Long bookingId, Long currentUserId, String currentRole) {
        if ("ADMIN".equals(currentRole) || "AGENT".equals(currentRole)) {
            return;
        }
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!booking.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("You can only view your own payments");
        }
    }

    public PageResponse<PaymentResponse> findMyPayments(Long userId, int page, int size) {
        var payments = paymentRepository.findByUserId(userId, page, size);
        var total = paymentRepository.countByUserId(userId);
        var content = payments.stream().map(p -> toResponse(p)).toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<PaymentResponse> findAll(int page, int size) {
        var payments = paymentRepository.findAll(page, size);
        var total = paymentRepository.count();
        var content = payments.stream().map(p -> toResponse(p)).toList();
        return buildPageResponse(content, page, size, total);
    }
}
