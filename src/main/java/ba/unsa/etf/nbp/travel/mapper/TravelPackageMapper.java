package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.response.AccommodationResponse;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageDetailResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageResponse;
import ba.unsa.etf.nbp.travel.model.entity.TravelPackageEntity;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TravelPackageMapper {

    public static TravelPackageResponse toResponse(TravelPackageEntity entity, String destinationName) {
        return new TravelPackageResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBasePrice(),
                entity.getMaxCapacity(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDestinationId(),
                destinationName
        );
    }

    public static TravelPackageDetailResponse toDetailResponse(TravelPackageEntity entity, String destinationName,
                                                        List<TransportResponse> transports,
                                                        List<AccommodationResponse> accommodations) {
        return new TravelPackageDetailResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBasePrice(),
                entity.getMaxCapacity(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDestinationId(),
                destinationName,
                transports,
                accommodations
        );
    }
}
