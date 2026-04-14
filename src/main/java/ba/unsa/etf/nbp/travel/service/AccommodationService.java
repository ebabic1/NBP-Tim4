package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.AccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.response.AccommodationResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.AccommodationRepository;
import ba.unsa.etf.nbp.travel.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static ba.unsa.etf.nbp.travel.mapper.AccommodationMapper.toEntity;
import static ba.unsa.etf.nbp.travel.mapper.AccommodationMapper.toResponse;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final DestinationRepository destinationRepository;

    public PageResponse<AccommodationResponse> findAll(int page, int size) {
        var accommodations = accommodationRepository.findAllWithName(page, size);
        var total = accommodationRepository.count();
        var content = accommodations.stream()
                .map(a -> toResponse(a.entity(), a.destinationName()))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public AccommodationResponse findById(Long id) {
        var entity = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation", id));
        return toResponse(entity, getDestinationName(entity.getDestinationId()));
    }

    public PageResponse<AccommodationResponse> search(Long destinationId, String type, Integer minStars,
                                                      Integer maxStars, BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size) {
        var accommodations = accommodationRepository.search(destinationId, type, minStars, maxStars,
                minPrice, maxPrice, page, size);
        var total = accommodationRepository.countSearch(destinationId, type, minStars, maxStars, minPrice, maxPrice);
        var content = accommodations.stream()
                .map(a -> toResponse(a, getDestinationName(a.getDestinationId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public AccommodationResponse create(AccommodationRequest request) {
        var entity = toEntity(request);
        var id = accommodationRepository.save(entity);
        entity.setId(id);
        return toResponse(entity, getDestinationName(entity.getDestinationId()));
    }

    public AccommodationResponse update(Long id, AccommodationRequest request) {
        var existing = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation", id));

        existing.setName(request.name());
        existing.setType(request.type());
        existing.setStars(request.stars());
        existing.setPhone(request.phone());
        existing.setEmail(request.email());
        existing.setPricePerNight(request.pricePerNight());
        existing.setCapacity(request.capacity());
        existing.setAddressId(request.addressId());
        existing.setDestinationId(request.destinationId());

        accommodationRepository.update(existing);
        return toResponse(existing, getDestinationName(existing.getDestinationId()));
    }

    public void delete(Long id) {
        accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation", id));
        accommodationRepository.deleteById(id);
    }

    private String getDestinationName(Long destinationId) {
        return destinationRepository.findById(destinationId)
                .map(d -> d.getName())
                .orElse(null);
    }
}
