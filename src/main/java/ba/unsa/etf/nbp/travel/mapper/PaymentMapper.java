package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.response.PaymentResponse;
import ba.unsa.etf.nbp.travel.model.entity.PaymentEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaymentMapper {

    public static PaymentResponse toResponse(PaymentEntity e) {
        return new PaymentResponse(
                e.getId(),
                e.getBookingId(),
                e.getDiscountId(),
                e.getAmount(),
                e.getDiscountAmount(),
                e.getFinalAmount(),
                e.getPaymentDate(),
                e.getMethod(),
                e.getStatus()
        );
    }
}
