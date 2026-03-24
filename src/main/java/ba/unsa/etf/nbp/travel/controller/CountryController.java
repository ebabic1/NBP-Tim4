package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.service.CityService;
import ba.unsa.etf.nbp.travel.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;
    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CountryResponse>> findAll() {
        var countries = countryService.findAll();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryResponse> findById(@PathVariable Long id) {
        var country = countryService.findById(id);
        return ResponseEntity.ok(country);
    }

    @GetMapping("/{id}/cities")
    public ResponseEntity<List<CityResponse>> getCitiesByCountryId(@PathVariable Long id) {
        var cities = cityService.findByCountryId(id);
        return ResponseEntity.ok(cities);
    }
}
