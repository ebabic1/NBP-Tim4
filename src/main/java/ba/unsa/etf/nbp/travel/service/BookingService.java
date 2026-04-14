package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.BookingRequest;
import ba.unsa.etf.nbp.travel.dto.response.BookingResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.exception.BadRequestException;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ForbiddenException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.enums.BookingType;
import ba.unsa.etf.nbp.travel.repository.AccommodationRepository;
import ba.unsa.etf.nbp.travel.repository.BookingRepository;
import ba.unsa.etf.nbp.travel.repository.PaymentRepository;
import ba.unsa.etf.nbp.travel.repository.TransportRepository;
import ba.unsa.etf.nbp.travel.repository.TravelPackageRepository;
import ba.unsa.etf.nbp.travel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static ba.unsa.etf.nbp.travel.mapper.BookingMapper.toEntity;
import static ba.unsa.etf.nbp.travel.mapper.BookingMapper.toResponse;
import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.CANCELLED;
import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.CONFIRMED;
import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.PENDING;
import static ba.unsa.etf.nbp.travel.model.enums.PaymentStatus.COMPLETED;
import static ba.unsa.etf.nbp.travel.model.enums.BookingType.ACCOMMODATION;
import static ba.unsa.etf.nbp.travel.model.enums.BookingType.TRANSPORT;
import static ba.unsa.etf.nbp.travel.model.enums.BookingType.TRAVEL_PACKAGE;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final AccommodationRepository accommodationRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public BookingResponse create(Long userId, BookingRequest request) {
        var type = parseBookingType(request.bookingType());
        validateForeignKeys(type, request);

        var totalPrice = calculateTotalPrice(type, request);

        var entity = toEntity(request, userId, totalPrice);
        var id = bookingRepository.save(entity);

        entity.setId(id);
        var saved = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return toResponse(saved);
    }

    public BookingResponse findById(Long id) {
        var entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return toResponse(entity);
    }

    public PageResponse<BookingResponse> findByUserId(Long userId, int page, int size) {
        var bookings = bookingRepository.findByUserId(userId, page, size);
        var total = bookingRepository.countByUserId(userId);
        var content = bookings.stream()
                .map(b -> toResponse(b))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<BookingResponse> findAll(int page, int size) {
        var bookings = bookingRepository.findAll(page, size);
        var total = bookingRepository.count();
        var content = bookings.stream()
                .map(b -> toResponse(b))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    @Transactional
    public BookingResponse confirm(Long id) {
        var entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!PENDING.name().equals(entity.getStatus())) {
            throw new BadRequestException("Booking can only be confirmed when status is PENDING");
        }

        var payment = paymentRepository.findByBookingId(id)
                .orElseThrow(() -> new BadRequestException("Cannot confirm booking without payment"));
        if (!COMPLETED.name().equals(payment.getStatus())) {
            throw new BadRequestException("Cannot confirm booking with non-completed payment");
        }

        bookingRepository.updateStatus(id, CONFIRMED.name());

        entity.setStatus(CONFIRMED.name());
        return toResponse(entity);
    }

    @Transactional
    public BookingResponse cancel(Long id, Long currentUserId) {
        var entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!entity.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("You can only cancel your own bookings");
        }

        if (CANCELLED.name().equals(entity.getStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }

        bookingRepository.updateStatus(id, CANCELLED.name());

        entity.setStatus(CANCELLED.name());
        return toResponse(entity);
    }

    private BookingType parseBookingType(String bookingType) {
        try {
            return BookingType.valueOf(bookingType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid booking type: " + bookingType);
        }
    }

    private void validateForeignKeys(BookingType type, BookingRequest request) {
        if (type == TRAVEL_PACKAGE) {
            if (isNull(request.travelPackageId())) {
                throw new BadRequestException("travelPackageId is required for TRAVEL_PACKAGE booking");
            }
            if (nonNull(request.accommodationId()) || nonNull(request.transportId())) {
                throw new BadRequestException("Only travelPackageId should be set for TRAVEL_PACKAGE booking");
            }
        } else if (type == ACCOMMODATION) {
            if (isNull(request.accommodationId())) {
                throw new BadRequestException("accommodationId is required for ACCOMMODATION booking");
            }
            if (nonNull(request.travelPackageId()) || nonNull(request.transportId())) {
                throw new BadRequestException("Only accommodationId should be set for ACCOMMODATION booking");
            }
        } else if (type == TRANSPORT) {
            if (isNull(request.transportId())) {
                throw new BadRequestException("transportId is required for TRANSPORT booking");
            }
            if (nonNull(request.travelPackageId()) || nonNull(request.accommodationId())) {
                throw new BadRequestException("Only transportId should be set for TRANSPORT booking");
            }
        }
    }

    private BigDecimal calculateTotalPrice(BookingType type, BookingRequest request) {
        if (type == TRAVEL_PACKAGE) {
            var travelPackage = travelPackageRepository.findById(request.travelPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", request.travelPackageId()));

            var confirmedCount = bookingRepository.countConfirmedByTravelPackageId(request.travelPackageId());
            if (confirmedCount >= travelPackage.getMaxCapacity()) {
                throw new ConflictException("No available capacity");
            }

            return travelPackage.getBasePrice();
        } else if (type == ACCOMMODATION) {
            var accommodation = accommodationRepository.findById(request.accommodationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation", request.accommodationId()));

            var confirmedCount = bookingRepository.countConfirmedByAccommodationId(request.accommodationId());
            if (confirmedCount >= accommodation.getCapacity()) {
                throw new ConflictException("No available capacity");
            }

            return accommodation.getPricePerNight();
        } else {
            var transport = transportRepository.findById(request.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transport", request.transportId()));

            var confirmedCount = bookingRepository.countConfirmedByTransportId(request.transportId());
            if (confirmedCount >= transport.getCapacity()) {
                throw new ConflictException("No available capacity");
            }

            return transport.getPrice();
        }
    }
}
