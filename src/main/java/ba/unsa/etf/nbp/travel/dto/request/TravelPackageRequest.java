package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TravelPackageRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal basePrice,
        @NotNull @Min(1) Integer maxCapacity,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Long destinationId
) {
}
