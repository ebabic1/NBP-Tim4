package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TravelPackageDetailResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Integer maxCapacity,
        LocalDate startDate,
        LocalDate endDate,
        Long destinationId,
        String destinationName,
        List<TransportResponse> transports,
        List<AccommodationResponse> accommodations
) {
}
