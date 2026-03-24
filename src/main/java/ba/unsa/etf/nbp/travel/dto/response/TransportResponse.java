package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransportResponse(
        Long id,
        String type,
        String provider,
        LocalDate departureDate,
        LocalDate arrivalDate,
        String origin,
        BigDecimal price,
        Integer capacity,
        Long destinationId,
        String destinationName
) {
}
