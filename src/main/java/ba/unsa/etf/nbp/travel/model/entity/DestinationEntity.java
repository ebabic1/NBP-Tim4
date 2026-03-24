package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationEntity {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long cityId;
}
