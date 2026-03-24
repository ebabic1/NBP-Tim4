package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank String method,
        String discountCode
) {
}
