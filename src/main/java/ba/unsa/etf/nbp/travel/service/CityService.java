package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.CityRequest;
import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.CityEntity;
import ba.unsa.etf.nbp.travel.repository.CityRepository;
import ba.unsa.etf.nbp.travel.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public CityResponse create(CityRequest request) {
        countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.countryId()));

        var entity = CityEntity.builder()
                .id(null)
                .name(request.name())
                .countryId(request.countryId())
                .build();
        var id = cityRepository.save(entity);
        entity.setId(id);
        return toResponse(entity);
    }

    public CityResponse update(Long id, CityRequest request) {
        var existing = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));

        countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.countryId()));

        existing.setName(request.name());
        existing.setCountryId(request.countryId());
        cityRepository.update(existing);
        return toResponse(existing);
    }

    public void delete(Long id) {
        cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
        try {
            cityRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("City is referenced by other records and cannot be deleted");
        }
    }

    private CityResponse toResponse(CityEntity city) {
        var countryName = countryRepository.findById(city.getCountryId())
                .map(c -> c.getName())
                .orElse(null);
        return new CityResponse(city.getId(), city.getName(), city.getCountryId(), countryName);
    }
}
