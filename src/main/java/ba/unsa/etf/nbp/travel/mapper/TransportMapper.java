package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.request.TransportRequest;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.model.entity.TransportEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TransportMapper {

    public static TransportResponse toResponse(TransportEntity entity, String destinationName) {
        return new TransportResponse(
                entity.getId(),
                entity.getType(),
                entity.getProvider(),
                entity.getDepartureDate(),
                entity.getArrivalDate(),
                entity.getOrigin(),
                entity.getPrice(),
                entity.getCapacity(),
                entity.getDestinationId(),
                destinationName
        );
    }

    public static TransportEntity toEntity(TransportRequest request) {
        return TransportEntity.builder()
                .id(null)
                .type(request.type())
                .provider(request.provider())
                .departureDate(request.departureDate())
                .arrivalDate(request.arrivalDate())
                .origin(request.origin())
                .price(request.price())
                .capacity(request.capacity())
                .destinationId(request.destinationId())
                .build();
    }
}
