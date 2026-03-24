package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.DestinationRequest;
import ba.unsa.etf.nbp.travel.dto.response.DestinationResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.CityRepository;
import ba.unsa.etf.nbp.travel.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ba.unsa.etf.nbp.travel.mapper.DestinationMapper.toEntity;
import static ba.unsa.etf.nbp.travel.mapper.DestinationMapper.toResponse;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final CityRepository cityRepository;

    public PageResponse<DestinationResponse> findAll(int page, int size) {
        var destinations = destinationRepository.findAll(page, size);
        var total = destinationRepository.count();
        var content = destinations.stream()
                .map(d -> toResponse(d, getCityName(d.getCityId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public DestinationResponse findById(Long id) {
        var entity = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", id));
        return toResponse(entity, getCityName(entity.getCityId()));
    }

    public List<DestinationResponse> findByCityId(Long cityId) {
        return destinationRepository.findByCityId(cityId).stream()
                .map(d -> toResponse(d, getCityName(d.getCityId())))
                .toList();
    }

    public DestinationResponse create(DestinationRequest request) {
        var entity = toEntity(request);
        var id = destinationRepository.save(entity);
        entity.setId(id);
        return toResponse(entity, getCityName(entity.getCityId()));
    }

    public DestinationResponse update(Long id, DestinationRequest request) {
        var existing = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", id));

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setImageUrl(request.imageUrl());
        existing.setCityId(request.cityId());

        destinationRepository.update(existing);
        return toResponse(existing, getCityName(existing.getCityId()));
    }

    public void delete(Long id) {
        destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", id));
        destinationRepository.deleteById(id);
    }

    private String getCityName(Long cityId) {
        return cityRepository.findById(cityId)
                .map(c -> c.getName())
                .orElse(null);
    }
}
