package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageAccommodationEntity {

    private Long travelPackageId;
    private Long accommodationId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer nights;
}
