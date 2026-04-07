package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.request.PackageAccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.request.PackageTransportRequest;
import ba.unsa.etf.nbp.travel.dto.request.TravelPackageRequest;
import ba.unsa.etf.nbp.travel.dto.response.AccommodationResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageDetailResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageResponse;
import ba.unsa.etf.nbp.travel.exception.ResourceNotFoundException;
import ba.unsa.etf.nbp.travel.mapper.AccommodationMapper;
import ba.unsa.etf.nbp.travel.mapper.TransportMapper;
import ba.unsa.etf.nbp.travel.model.entity.PackageAccommodationEntity;
import ba.unsa.etf.nbp.travel.model.entity.PackageTransportEntity;
import ba.unsa.etf.nbp.travel.model.entity.TravelPackageEntity;
import ba.unsa.etf.nbp.travel.repository.AccommodationRepository;
import ba.unsa.etf.nbp.travel.repository.DestinationRepository;
import ba.unsa.etf.nbp.travel.repository.PackageAccommodationRepository;
import ba.unsa.etf.nbp.travel.repository.PackageTransportRepository;
import ba.unsa.etf.nbp.travel.repository.TransportRepository;
import ba.unsa.etf.nbp.travel.repository.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static ba.unsa.etf.nbp.travel.mapper.TravelPackageMapper.toDetailResponse;
import static ba.unsa.etf.nbp.travel.mapper.TravelPackageMapper.toResponse;
import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class TravelPackageService {

    private final TravelPackageRepository travelPackageRepository;
    private final PackageTransportRepository packageTransportRepository;
    private final PackageAccommodationRepository packageAccommodationRepository;
    private final DestinationRepository destinationRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    public PageResponse<TravelPackageResponse> findAll(int page, int size) {
        var packages = travelPackageRepository.findAll(page, size);
        var total = travelPackageRepository.count();
        var content = packages.stream()
                .map(p -> toResponse(p, getDestinationName(p.getDestinationId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public TravelPackageDetailResponse findById(Long id) {
        var entity = travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", id));

        var transports = getTransportResponses(id);
        var accommodations = getAccommodationResponses(id);

        return toDetailResponse(entity, getDestinationName(entity.getDestinationId()), transports, accommodations);
    }

    public PageResponse<TravelPackageResponse> search(Long destinationId, Long cityId, Long countryId,
                                                      LocalDate startDate, LocalDate endDate,
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size) {
        var packages = travelPackageRepository.search(destinationId, cityId, countryId, startDate, endDate,
                minPrice, maxPrice, page, size);
        var total = travelPackageRepository.countSearch(destinationId, cityId, countryId,
                startDate, endDate, minPrice, maxPrice);
        var content = packages.stream()
                .map(p -> toResponse(p, getDestinationName(p.getDestinationId())))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public TravelPackageResponse create(TravelPackageRequest request) {
        var entity = TravelPackageEntity.builder()
                .id(null)
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .maxCapacity(request.maxCapacity())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .destinationId(request.destinationId())
                .build();

        var id = travelPackageRepository.save(entity);
        entity.setId(id);
        return toResponse(entity, getDestinationName(entity.getDestinationId()));
    }

    public TravelPackageResponse update(Long id, TravelPackageRequest request) {
        var existing = travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", id));

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setBasePrice(request.basePrice());
        existing.setMaxCapacity(request.maxCapacity());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());
        existing.setDestinationId(request.destinationId());

        travelPackageRepository.update(existing);
        return toResponse(existing, getDestinationName(existing.getDestinationId()));
    }

    public void delete(Long id) {
        travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", id));

        packageTransportRepository.deleteAllByPackageId(id);
        packageAccommodationRepository.deleteAllByPackageId(id);
        travelPackageRepository.deleteById(id);
    }

    public void addTransport(Long packageId, PackageTransportRequest request) {
        travelPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", packageId));
        transportRepository.findById(request.transportId())
                .orElseThrow(() -> new ResourceNotFoundException("Transport", request.transportId()));

        var entity = PackageTransportEntity.builder()
                .travelPackageId(packageId)
                .transportId(request.transportId())
                .transportRole(request.transportRole())
                .sequenceOrder(request.sequenceOrder())
                .build();

        packageTransportRepository.save(entity);
    }

    public void removeTransport(Long packageId, Long transportId) {
        packageTransportRepository.deleteByPackageIdAndTransportId(packageId, transportId);
    }

    public void addAccommodation(Long packageId, PackageAccommodationRequest request) {
        travelPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage", packageId));
        accommodationRepository.findById(request.accommodationId())
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation", request.accommodationId()));

        var entity = PackageAccommodationEntity.builder()
                .travelPackageId(packageId)
                .accommodationId(request.accommodationId())
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .nights(request.nights())
                .build();

        packageAccommodationRepository.save(entity);
    }

    public void removeAccommodation(Long packageId, Long accommodationId) {
        packageAccommodationRepository.deleteByPackageIdAndAccommodationId(packageId, accommodationId);
    }

    private String getDestinationName(Long destinationId) {
        return destinationRepository.findById(destinationId)
                .map(d -> d.getName())
                .orElse(null);
    }

    private List<TransportResponse> getTransportResponses(Long packageId) {
        return packageTransportRepository.findByPackageId(packageId).stream()
                .map(pt -> {
                    var transport = transportRepository.findById(pt.getTransportId()).orElse(null);
                    if (isNull(transport)) {
                        return null;
                    }
                    return TransportMapper.toResponse(transport, getDestinationName(transport.getDestinationId()));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AccommodationResponse> getAccommodationResponses(Long packageId) {
        return packageAccommodationRepository.findByPackageId(packageId).stream()
                .map(pa -> {
                    var accommodation = accommodationRepository.findById(pa.getAccommodationId()).orElse(null);
                    if (isNull(accommodation)) {
                        return null;
                    }
                    return AccommodationMapper.toResponse(
                            accommodation, getDestinationName(accommodation.getDestinationId()));
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
