package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.PackageAccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.request.PackageTransportRequest;
import ba.unsa.etf.nbp.travel.dto.request.TravelPackageRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageDetailResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.TravelPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/travel-packages")
@RequiredArgsConstructor
public class TravelPackageController {

    private final TravelPackageService travelPackageService;

    @GetMapping
    public ResponseEntity<PageResponse<TravelPackageResponse>> search(
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(travelPackageService.search(destinationId, cityId, startDate, endDate,
                minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackageDetailResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(travelPackageService.findById(id));
    }

    @PostMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<TravelPackageResponse> create(@Valid @RequestBody TravelPackageRequest request) {
        var response = travelPackageService.create(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<TravelPackageResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody TravelPackageRequest request) {
        return ResponseEntity.ok(travelPackageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        travelPackageService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("/{id}/transports")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> addTransport(@PathVariable Long id,
                                             @Valid @RequestBody PackageTransportRequest request) {
        travelPackageService.addTransport(id, request);
        return ResponseEntity.status(CREATED).build();
    }

    @DeleteMapping("/{id}/transports/{transportId}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> removeTransport(@PathVariable Long id, @PathVariable Long transportId) {
        travelPackageService.removeTransport(id, transportId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("/{id}/accommodations")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> addAccommodation(@PathVariable Long id,
                                                 @Valid @RequestBody PackageAccommodationRequest request) {
        travelPackageService.addAccommodation(id, request);
        return ResponseEntity.status(CREATED).build();
    }

    @DeleteMapping("/{id}/accommodations/{accommodationId}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> removeAccommodation(@PathVariable Long id, @PathVariable Long accommodationId) {
        travelPackageService.removeAccommodation(id, accommodationId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
