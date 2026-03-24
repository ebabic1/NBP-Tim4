package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PackageAccommodationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class PackageAccommodationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<PackageAccommodationEntity> ROW_MAPPER = (rs, rowNum) -> PackageAccommodationEntity.builder()
            .travelPackageId(rs.getLong("TRAVEL_PACKAGE_ID"))
            .accommodationId(rs.getLong("ACCOMMODATION_ID"))
            .checkIn(nonNull(rs.getDate("CHECK_IN")) ? rs.getDate("CHECK_IN").toLocalDate() : null)
            .checkOut(nonNull(rs.getDate("CHECK_OUT")) ? rs.getDate("CHECK_OUT").toLocalDate() : null)
            .nights(rs.getInt("NIGHTS"))
            .build();

    private static final String SELECT_BY_PACKAGE_ID =
            "SELECT * FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = :packageId ORDER BY CHECK_IN";

    private static final String INSERT =
            """
            INSERT INTO NBP_PACKAGE_ACCOMMODATION (TRAVEL_PACKAGE_ID, ACCOMMODATION_ID, CHECK_IN, CHECK_OUT, NIGHTS)
            VALUES (:travelPackageId, :accommodationId, :checkIn, :checkOut, :nights)
            """;

    private static final String DELETE_BY_PACKAGE_AND_ACCOMMODATION =
            "DELETE FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = :packageId AND ACCOMMODATION_ID = :accommodationId";

    private static final String DELETE_ALL_BY_PACKAGE_ID =
            "DELETE FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = :packageId";

    public List<PackageAccommodationEntity> findByPackageId(Long packageId) {
        return jdbcTemplate.query(SELECT_BY_PACKAGE_ID, Map.of("packageId", packageId), ROW_MAPPER);
    }

    public void save(PackageAccommodationEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("travelPackageId", entity.getTravelPackageId())
                .addValue("accommodationId", entity.getAccommodationId())
                .addValue("checkIn", entity.getCheckIn())
                .addValue("checkOut", entity.getCheckOut())
                .addValue("nights", entity.getNights());

        jdbcTemplate.update(INSERT, params);
    }

    public void deleteByPackageIdAndAccommodationId(Long packageId, Long accommodationId) {
        jdbcTemplate.update(DELETE_BY_PACKAGE_AND_ACCOMMODATION,
                Map.of("packageId", packageId, "accommodationId", accommodationId));
    }

    public void deleteAllByPackageId(Long packageId) {
        jdbcTemplate.update(DELETE_ALL_BY_PACKAGE_ID, Map.of("packageId", packageId));
    }
}
