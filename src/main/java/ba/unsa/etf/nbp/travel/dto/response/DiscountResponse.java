package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountResponse(
        Long id,
        String code,
        BigDecimal percentage,
        LocalDate validFrom,
        LocalDate validTo,
        String description
) {
}
