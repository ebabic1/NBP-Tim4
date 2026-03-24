package ba.unsa.etf.nbp.travel.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SearchRequest(
        Long destinationId,
        Long cityId,
        Long countryId,
        String type,
        Integer minStars,
        Integer maxStars,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        LocalDate startDate,
        LocalDate endDate,
        Integer page,
        Integer size
) {
    public SearchRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }
    }
}
