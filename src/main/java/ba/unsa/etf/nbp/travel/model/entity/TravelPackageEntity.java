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
public class TravelPackageEntity {

    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer maxCapacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long destinationId;
}
