package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    private Long id;
    private Long userId;
    private String bookingType;
    private LocalDate bookingDate;
    private String status;
    private BigDecimal totalPrice;
    private Long travelPackageId;
    private Long accommodationId;
    private Long transportId;
}
