package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PackageTransportRequest(
        @NotNull Long transportId,
        @NotBlank String transportRole,
        @NotNull Integer sequenceOrder
) {
}
