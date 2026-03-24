package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long bookingId,
        Long discountId,
        BigDecimal amount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        LocalDate paymentDate,
        String method,
        String status
) {
}
