package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DestinationRequest(
        @NotBlank String name,
        String description,
        String imageUrl,
        @NotNull Long cityId
) {
}
