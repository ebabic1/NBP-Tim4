package ba.unsa.etf.nbp.travel.dto.response;

public record DestinationResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        Long cityId,
        String cityName
) {
}
