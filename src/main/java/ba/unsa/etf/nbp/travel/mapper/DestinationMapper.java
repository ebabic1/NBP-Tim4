package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.request.DestinationRequest;
import ba.unsa.etf.nbp.travel.dto.response.DestinationResponse;
import ba.unsa.etf.nbp.travel.model.entity.DestinationEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DestinationMapper {

    public static DestinationResponse toResponse(DestinationEntity entity, String cityName) {
        return new DestinationResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCityId(),
                cityName
        );
    }

    public static DestinationEntity toEntity(DestinationRequest request) {
        return DestinationEntity.builder()
                .id(null)
                .name(request.name())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .cityId(request.cityId())
                .build();
    }
}
