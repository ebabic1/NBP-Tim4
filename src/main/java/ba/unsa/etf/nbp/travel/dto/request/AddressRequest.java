package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotBlank String street,
        String zipCode,
        @NotNull Long cityId
) {
}
