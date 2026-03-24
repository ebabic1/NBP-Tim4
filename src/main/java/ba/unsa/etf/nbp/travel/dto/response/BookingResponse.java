package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long userId,
        String bookingType,
        LocalDate bookingDate,
        String status,
        BigDecimal totalPrice,
        Long travelPackageId,
        Long accommodationId,
        Long transportId
) {
}
