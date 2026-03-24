package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TravelPackageResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Integer maxCapacity,
        LocalDate startDate,
        LocalDate endDate,
        Long destinationId,
        String destinationName
) {
}
