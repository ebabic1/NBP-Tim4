package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.TransportRequest;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.repository.DestinationRepository;
import ba.unsa.etf.nbp.travel.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import static ba.unsa.etf.nbp.travel.mapper.TransportMapper.toEntity;
import static ba.unsa.etf.nbp.travel.mapper.TransportMapper.toResponse;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final DestinationRepository destinationRepository;

    public PageResponse<TransportResponse> findAll(int page, int size) {
        var transports = transportRepository.findAll(page, size);
        var total = transportRepository.count();
        var content = transports.stream()
                .map(t -> toResponse(t, getDestinationName(t.getDestinationId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public TransportResponse findById(Long id) {
        var entity = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", id));
        return toResponse(entity, getDestinationName(entity.getDestinationId()));
    }

    public PageResponse<TransportResponse> search(Long destinationId, String type, LocalDate startDate,
                                                   LocalDate endDate, BigDecimal minPrice, BigDecimal maxPrice,
                                                   int page, int size) {
        var transports = transportRepository.search(destinationId, type, startDate, endDate,
                minPrice, maxPrice, page, size);
        var total = transportRepository.countSearch(destinationId, type, startDate, endDate, minPrice, maxPrice);
        var content = transports.stream()
                .map(t -> toResponse(t, getDestinationName(t.getDestinationId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public TransportResponse create(TransportRequest request) {
        var entity = toEntity(request);
        var id = transportRepository.save(entity);
        entity.setId(id);
        return toResponse(entity, getDestinationName(entity.getDestinationId()));
    }

    public TransportResponse update(Long id, TransportRequest request) {
        var existing = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", id));

        existing.setType(request.type());
        existing.setProvider(request.provider());
        existing.setDepartureDate(request.departureDate());
        existing.setArrivalDate(request.arrivalDate());
        existing.setOrigin(request.origin());
        existing.setPrice(request.price());
        existing.setCapacity(request.capacity());
        existing.setDestinationId(request.destinationId());

        transportRepository.update(existing);
        return toResponse(existing, getDestinationName(existing.getDestinationId()));
    }

    public void delete(Long id) {
        transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", id));
        transportRepository.deleteById(id);
    }

    private String getDestinationName(Long destinationId) {
        return destinationRepository.findById(destinationId)
                .map(d -> d.getName())
                .orElse(null);
    }
}
