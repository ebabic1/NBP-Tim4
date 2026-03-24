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
public class DiscountEntity {

    private Long id;
    private String code;
    private BigDecimal percentage;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String description;
}
