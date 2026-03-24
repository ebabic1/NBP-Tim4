package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.CityEntity;
import ba.unsa.etf.nbp.travel.repository.CityRepository;
import ba.unsa.etf.nbp.travel.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    public List<CityResponse> findAll() {
        return cityRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CityResponse findById(Long id) {
        var city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
        return toResponse(city);
    }

    public List<CityResponse> findByCountryId(Long countryId) {
        return cityRepository.findByCountryId(countryId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CityResponse toResponse(CityEntity city) {
        var countryName = countryRepository.findById(city.getCountryId())
                .map(c -> c.getName())
                .orElse(null);
        return new CityResponse(city.getId(), city.getName(), city.getCountryId(), countryName);
    }
}
