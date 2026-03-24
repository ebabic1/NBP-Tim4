package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.request.AccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.response.AccommodationResponse;
import ba.unsa.etf.nbp.travel.model.entity.AccommodationEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccommodationMapper {

    public static AccommodationResponse toResponse(AccommodationEntity entity, String destinationName) {
        return new AccommodationResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getStars(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getPricePerNight(),
                entity.getCapacity(),
                entity.getAddressId(),
                entity.getDestinationId(),
                destinationName
        );
    }

    public static AccommodationEntity toEntity(AccommodationRequest request) {
        return AccommodationEntity.builder()
                .id(null)
                .name(request.name())
                .type(request.type())
                .stars(request.stars())
                .phone(request.phone())
                .email(request.email())
                .pricePerNight(request.pricePerNight())
                .capacity(request.capacity())
                .addressId(request.addressId())
                .destinationId(request.destinationId())
                .build();
    }
}
