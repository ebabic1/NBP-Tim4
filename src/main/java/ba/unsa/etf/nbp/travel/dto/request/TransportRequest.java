package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransportRequest(
        @NotBlank String type,
        @NotBlank String provider,
        @NotNull LocalDate departureDate,
        @NotNull LocalDate arrivalDate,
        @NotBlank String origin,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull @Min(1) Integer capacity,
        @NotNull Long destinationId
) {
}
