package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.request.BookingRequest;
import ba.unsa.etf.nbp.travel.dto.response.BookingResponse;
import ba.unsa.etf.nbp.travel.model.entity.BookingEntity;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

import static ba.unsa.etf.nbp.travel.model.enums.BookingStatus.PENDING;

@UtilityClass
public class BookingMapper {

    public static BookingResponse toResponse(BookingEntity e) {
        return new BookingResponse(
                e.getId(),
                e.getUserId(),
                e.getBookingType(),
                e.getBookingDate(),
                e.getStatus(),
                e.getTotalPrice(),
                e.getTravelPackageId(),
                e.getAccommodationId(),
                e.getTransportId()
        );
    }

    public static BookingEntity toEntity(BookingRequest r, Long userId, BigDecimal totalPrice) {
        return BookingEntity.builder()
                .id(null)
                .userId(userId)
                .bookingType(r.bookingType())
                .bookingDate(null)
                .status(PENDING.name())
                .totalPrice(totalPrice)
                .travelPackageId(r.travelPackageId())
                .accommodationId(r.accommodationId())
                .transportId(r.transportId())
                .build();
    }
}
