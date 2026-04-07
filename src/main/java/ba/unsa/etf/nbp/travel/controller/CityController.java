package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.CityRequest;
import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.CityService;
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
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> findAll() {
        var cities = cityService.findAll();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> findById(@PathVariable Long id) {
        var city = cityService.findById(id);
        return ResponseEntity.ok(city);
    }

    @PostMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<CityResponse> create(@Valid @RequestBody CityRequest request) {
        var response = cityService.create(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<CityResponse> update(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(cityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
