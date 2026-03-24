package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.TravelPackageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class TravelPackageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<TravelPackageEntity> ROW_MAPPER = (rs, rowNum) -> TravelPackageEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .description(rs.getString("DESCRIPTION"))
            .basePrice(rs.getBigDecimal("BASE_PRICE"))
            .maxCapacity(rs.getInt("MAX_CAPACITY"))
            .startDate(nonNull(rs.getDate("START_DATE")) ? rs.getDate("START_DATE").toLocalDate() : null)
            .endDate(nonNull(rs.getDate("END_DATE")) ? rs.getDate("END_DATE").toLocalDate() : null)
            .destinationId(rs.getLong("DESTINATION_ID"))
            .build();

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_TRAVEL_PACKAGE ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_TRAVEL_PACKAGE WHERE ID = :id";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_TRAVEL_PACKAGE WHERE DESTINATION_ID = :destinationId ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_TRAVEL_PACKAGE_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_TRAVEL_PACKAGE (ID, NAME, DESCRIPTION, BASE_PRICE, MAX_CAPACITY, START_DATE, END_DATE, DESTINATION_ID)
            VALUES (:id, :name, :description, :basePrice, :maxCapacity, :startDate, :endDate, :destinationId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_TRAVEL_PACKAGE
            SET NAME = :name, DESCRIPTION = :description, BASE_PRICE = :basePrice, MAX_CAPACITY = :maxCapacity,
                START_DATE = :startDate, END_DATE = :endDate, DESTINATION_ID = :destinationId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_TRAVEL_PACKAGE WHERE ID = :id";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_TRAVEL_PACKAGE";

    public List<TravelPackageEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public Optional<TravelPackageEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<TravelPackageEntity> findByDestinationId(Long destinationId) {
        return jdbcTemplate.query(SELECT_BY_DESTINATION_ID, Map.of("destinationId", destinationId), ROW_MAPPER);
    }

    public List<TravelPackageEntity> search(Long destinationId, Long cityId, LocalDate startDate, LocalDate endDate,
                                            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("tp.*", destinationId, cityId, startDate, endDate, minPrice, maxPrice, params);
        var offset = page * size;
        params.addValue("offset", offset);
        params.addValue("size", size);
        sql += " ORDER BY tp.ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public long countSearch(Long destinationId, Long cityId, LocalDate startDate, LocalDate endDate,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("COUNT(*)", destinationId, cityId, startDate, endDate, minPrice, maxPrice, params);
        var result = jdbcTemplate.queryForObject(sql, params, Long.class);
        return nonNull(result) ? result : 0L;
    }

    private String buildSearchQuery(String selectClause, Long destinationId, Long cityId, LocalDate startDate,
                                    LocalDate endDate, BigDecimal minPrice, BigDecimal maxPrice,
                                    MapSqlParameterSource params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_TRAVEL_PACKAGE tp");

        if (nonNull(cityId)) {
            sql.append(" JOIN NBP_DESTINATION d ON tp.DESTINATION_ID = d.ID");
        }

        sql.append(" WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND tp.DESTINATION_ID = :destinationId");
            params.addValue("destinationId", destinationId);
        }
        if (nonNull(cityId)) {
            sql.append(" AND d.CITY_ID = :cityId");
            params.addValue("cityId", cityId);
        }
        if (nonNull(startDate)) {
            sql.append(" AND tp.START_DATE >= :startDate");
            params.addValue("startDate", startDate);
        }
        if (nonNull(endDate)) {
            sql.append(" AND tp.END_DATE <= :endDate");
            params.addValue("endDate", endDate);
        }
        if (nonNull(minPrice)) {
            sql.append(" AND tp.BASE_PRICE >= :minPrice");
            params.addValue("minPrice", minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND tp.BASE_PRICE <= :maxPrice");
            params.addValue("maxPrice", maxPrice);
        }

        return sql.toString();
    }

    public Long save(TravelPackageEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("basePrice", entity.getBasePrice())
                .addValue("maxCapacity", entity.getMaxCapacity())
                .addValue("startDate", entity.getStartDate())
                .addValue("endDate", entity.getEndDate())
                .addValue("destinationId", entity.getDestinationId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(TravelPackageEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("basePrice", entity.getBasePrice())
                .addValue("maxCapacity", entity.getMaxCapacity())
                .addValue("startDate", entity.getStartDate())
                .addValue("endDate", entity.getEndDate())
                .addValue("destinationId", entity.getDestinationId());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }
}
