package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccommodationRequest(
        @NotBlank String name,
        @NotBlank String type,
        @Min(1) @Max(5) Integer stars,
        String phone,
        @Email String email,
        @NotNull @DecimalMin("0.01") BigDecimal pricePerNight,
        @NotNull @Min(1) Integer capacity,
        @NotNull Long addressId,
        @NotNull Long destinationId
) {
}
