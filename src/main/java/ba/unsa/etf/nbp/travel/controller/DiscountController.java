package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.request.DiscountRequest;
import ba.unsa.etf.nbp.travel.dto.response.DiscountResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.DiscountService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<List<DiscountResponse>> findAll() {
        return ResponseEntity.ok(discountService.findAll());
    }

    @PostMapping
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<DiscountResponse> create(@Valid @RequestBody DiscountRequest request) {
        var response = discountService.create(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Role({"AGENT", "ADMIN"})
    public ResponseEntity<DiscountResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(discountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Role({"ADMIN"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("/validate")
    public ResponseEntity<DiscountResponse> validateCode(@RequestParam String code) {
        return ResponseEntity.ok(discountService.validateCode(code));
    }
}
