package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PackageAccommodationRequest(
        @NotNull Long accommodationId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @Min(1) Integer nights
) {
}
