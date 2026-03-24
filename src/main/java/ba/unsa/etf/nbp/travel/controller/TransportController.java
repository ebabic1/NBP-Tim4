package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.TransportRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.TransportService;
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
@RequestMapping("/api/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @GetMapping
    public ResponseEntity<PageResponse<TransportResponse>> search(
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.search(destinationId, type, startDate, endDate,
                minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transportService.findById(id));
    }

    @PostMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<TransportResponse> create(@Valid @RequestBody TransportRequest request) {
        var response = transportService.create(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<TransportResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody TransportRequest request) {
        return ResponseEntity.ok(transportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transportService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
