package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountRequest(
        @NotBlank String code,
        @NotNull @DecimalMin("0.01") @DecimalMax("100") BigDecimal percentage,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTo,
        String description
) {
}
