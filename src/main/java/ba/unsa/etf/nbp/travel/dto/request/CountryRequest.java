package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CountryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 10) String code
) {
}

