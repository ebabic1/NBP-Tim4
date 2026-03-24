package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageTransportEntity {

    private Long travelPackageId;
    private Long transportId;
    private String transportRole;
    private Integer sequenceOrder;
}
