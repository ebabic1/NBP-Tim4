package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
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
}
