package ba.unsa.etf.nbp.travel.dto.response;

public record CityResponse(
        Long id,
        String name,
        Long countryId,
        String countryName
) {
}
