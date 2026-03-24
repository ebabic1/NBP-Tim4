package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PackageTransportEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PackageTransportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<PackageTransportEntity> ROW_MAPPER = (rs, rowNum) -> PackageTransportEntity.builder()
            .travelPackageId(rs.getLong("TRAVEL_PACKAGE_ID"))
            .transportId(rs.getLong("TRANSPORT_ID"))
            .transportRole(rs.getString("TRANSPORT_ROLE"))
            .sequenceOrder(rs.getInt("SEQUENCE_ORDER"))
            .build();

    private static final String SELECT_BY_PACKAGE_ID =
            "SELECT * FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = :packageId ORDER BY SEQUENCE_ORDER";

    private static final String INSERT =
            """
            INSERT INTO NBP_PACKAGE_TRANSPORT (TRAVEL_PACKAGE_ID, TRANSPORT_ID, TRANSPORT_ROLE, SEQUENCE_ORDER)
            VALUES (:travelPackageId, :transportId, :transportRole, :sequenceOrder)
            """;

    private static final String DELETE_BY_PACKAGE_AND_TRANSPORT =
            "DELETE FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = :packageId AND TRANSPORT_ID = :transportId";

    private static final String DELETE_ALL_BY_PACKAGE_ID =
            "DELETE FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = :packageId";

    public List<PackageTransportEntity> findByPackageId(Long packageId) {
        return jdbcTemplate.query(SELECT_BY_PACKAGE_ID, Map.of("packageId", packageId), ROW_MAPPER);
    }

    public void save(PackageTransportEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("travelPackageId", entity.getTravelPackageId())
                .addValue("transportId", entity.getTransportId())
                .addValue("transportRole", entity.getTransportRole())
                .addValue("sequenceOrder", entity.getSequenceOrder());

        jdbcTemplate.update(INSERT, params);
    }

    public void deleteByPackageIdAndTransportId(Long packageId, Long transportId) {
        jdbcTemplate.update(DELETE_BY_PACKAGE_AND_TRANSPORT, Map.of("packageId", packageId, "transportId", transportId));
    }

    public void deleteAllByPackageId(Long packageId) {
        jdbcTemplate.update(DELETE_ALL_BY_PACKAGE_ID, Map.of("packageId", packageId));
    }
}
