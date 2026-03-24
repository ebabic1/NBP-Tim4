package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationEntity {

    private Long id;
    private String name;
    private String type;
    private Integer stars;
    private String phone;
    private String email;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private Long addressId;
    private Long destinationId;
}
