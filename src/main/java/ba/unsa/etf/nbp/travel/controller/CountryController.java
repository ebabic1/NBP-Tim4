package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.CountryRequest;
import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.CityService;
import ba.unsa.etf.nbp.travel.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

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

    @PostMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<CountryResponse> create(@Valid @RequestBody CountryRequest request) {
        var response = countryService.create(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<CountryResponse> update(@PathVariable Long id, @Valid @RequestBody CountryRequest request) {
        return ResponseEntity.ok(countryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        countryService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
