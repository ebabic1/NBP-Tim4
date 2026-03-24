package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.DiscountRequest;
import ba.unsa.etf.nbp.travel.dto.response.DiscountResponse;
import ba.unsa.etf.nbp.travel.exception.BadRequestException;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.model.entity.DiscountEntity;
import ba.unsa.etf.nbp.travel.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    public List<DiscountResponse> findAll() {
        return discountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DiscountResponse findById(Long id) {
        var entity = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", id));
        return toResponse(entity);
    }

    public DiscountResponse create(DiscountRequest request) {
        var entity = DiscountEntity.builder()
                .id(null)
                .code(request.code())
                .percentage(request.percentage())
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .description(request.description())
                .build();

        var id = discountRepository.save(entity);
        entity.setId(id);
        return toResponse(entity);
    }

    public DiscountResponse update(Long id, DiscountRequest request) {
        var existing = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", id));

        existing.setCode(request.code());
        existing.setPercentage(request.percentage());
        existing.setValidFrom(request.validFrom());
        existing.setValidTo(request.validTo());
        existing.setDescription(request.description());

        discountRepository.update(existing);
        return toResponse(existing);
    }

    public void delete(Long id) {
        discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", id));
        discountRepository.deleteById(id);
    }

    public DiscountResponse validateCode(String code) {
        var entity = discountRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Discount code not found: " + code));

        var now = LocalDate.now();
        if (now.isBefore(entity.getValidFrom()) || now.isAfter(entity.getValidTo())) {
            throw new BadRequestException("Discount code is expired or not yet valid: " + code);
        }

        return toResponse(entity);
    }

    private DiscountResponse toResponse(DiscountEntity entity) {
        return new DiscountResponse(
                entity.getId(),
                entity.getCode(),
                entity.getPercentage(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getDescription()
        );
    }
}
