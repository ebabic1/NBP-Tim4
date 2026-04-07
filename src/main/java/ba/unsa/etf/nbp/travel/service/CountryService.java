package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.CountryRequest;
import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.exception.ConflictException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.CountryEntity;
import ba.unsa.etf.nbp.travel.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public List<CountryResponse> findAll() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryResponse(c.getId(), c.getName(), c.getCode()))
                .toList();
    }

    public CountryResponse findById(Long id) {
        var country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
        return new CountryResponse(country.getId(), country.getName(), country.getCode());
    }

    public CountryResponse create(CountryRequest request) {
        var entity = CountryEntity.builder()
                .id(null)
                .name(request.name())
                .code(request.code())
                .build();
        var id = countryRepository.save(entity);
        entity.setId(id);
        return new CountryResponse(entity.getId(), entity.getName(), entity.getCode());
    }

    public CountryResponse update(Long id, CountryRequest request) {
        var existing = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));

        existing.setName(request.name());
        existing.setCode(request.code());
        countryRepository.update(existing);

        return new CountryResponse(existing.getId(), existing.getName(), existing.getCode());
    }

    public void delete(Long id) {
        countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
        try {
            countryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Country is referenced by other records and cannot be deleted");
        }
    }
}
