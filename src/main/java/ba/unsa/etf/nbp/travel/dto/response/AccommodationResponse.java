package ba.unsa.etf.nbp.travel.dto.response;

import java.math.BigDecimal;

public record AccommodationResponse(
        Long id,
        String name,
        String type,
        Integer stars,
        String phone,
        String email,
        BigDecimal pricePerNight,
        Integer capacity,
        Long addressId,
        Long destinationId,
        String destinationName
) {
}
