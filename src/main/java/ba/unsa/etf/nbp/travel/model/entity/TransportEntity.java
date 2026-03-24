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
public class TransportEntity {

    private Long id;
    private String type;
    private String provider;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private String origin;
    private BigDecimal price;
    private Integer capacity;
    private Long destinationId;
}
