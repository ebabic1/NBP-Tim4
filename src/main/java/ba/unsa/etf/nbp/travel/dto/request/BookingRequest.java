package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BookingRequest(
        @NotBlank String bookingType,
        Long travelPackageId,
        Long accommodationId,
        Long transportId
) {
}
