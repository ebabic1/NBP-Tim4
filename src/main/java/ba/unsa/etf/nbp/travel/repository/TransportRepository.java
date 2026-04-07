package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.TransportEntity;
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
public class TransportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<TransportEntity> ROW_MAPPER = (rs, rowNum) -> TransportEntity.builder()
            .id(rs.getLong("ID"))
            .type(rs.getString("TYPE"))
            .provider(rs.getString("PROVIDER"))
            .departureDate(nonNull(rs.getDate("DEPARTURE_DATE")) ? rs.getDate("DEPARTURE_DATE").toLocalDate() : null)
            .arrivalDate(nonNull(rs.getDate("ARRIVAL_DATE")) ? rs.getDate("ARRIVAL_DATE").toLocalDate() : null)
            .origin(rs.getString("ORIGIN"))
            .price(rs.getBigDecimal("PRICE"))
            .capacity(rs.getInt("CAPACITY"))
            .destinationId(rs.getLong("DESTINATION_ID"))
            .build();

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_TRANSPORT ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_TRANSPORT WHERE ID = :id";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_TRANSPORT WHERE DESTINATION_ID = :destinationId ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_TRANSPORT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_TRANSPORT (ID, TYPE, PROVIDER, DEPARTURE_DATE, ARRIVAL_DATE, ORIGIN, PRICE, CAPACITY, DESTINATION_ID)
            VALUES (:id, :type, :provider, :departureDate, :arrivalDate, :origin, :price, :capacity, :destinationId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_TRANSPORT
            SET TYPE = :type, PROVIDER = :provider, DEPARTURE_DATE = :departureDate, ARRIVAL_DATE = :arrivalDate,
                ORIGIN = :origin, PRICE = :price, CAPACITY = :capacity, DESTINATION_ID = :destinationId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_TRANSPORT WHERE ID = :id";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_TRANSPORT";

    public List<TransportEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public Optional<TransportEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<TransportEntity> findByDestinationId(Long destinationId) {
        return jdbcTemplate.query(SELECT_BY_DESTINATION_ID, Map.of("destinationId", destinationId), ROW_MAPPER);
    }

    public List<TransportEntity> search(Long destinationId, String type, String provider,
                                        LocalDate startDate, LocalDate endDate,
                                        BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("*", destinationId, type, provider, startDate, endDate, minPrice, maxPrice, params);
        var offset = page * size;
        params.addValue("offset", offset);
        params.addValue("size", size);
        sql += " ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public long countSearch(Long destinationId, String type, String provider,
                            LocalDate startDate, LocalDate endDate,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("COUNT(*)", destinationId, type, provider, startDate, endDate, minPrice, maxPrice, params);
        var result = jdbcTemplate.queryForObject(sql, params, Long.class);
        return nonNull(result) ? result : 0L;
    }

    private String buildSearchQuery(String selectClause, Long destinationId, String type, String provider,
                                    LocalDate startDate, LocalDate endDate,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    MapSqlParameterSource params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_TRANSPORT WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND DESTINATION_ID = :destinationId");
            params.addValue("destinationId", destinationId);
        }
        if (nonNull(type)) {
            sql.append(" AND TYPE = :type");
            params.addValue("type", type);
        }
        if (nonNull(provider) && !provider.isBlank()) {
            sql.append(" AND UPPER(PROVIDER) LIKE '%' || UPPER(:provider) || '%'");
            params.addValue("provider", provider.trim());
        }
        if (nonNull(startDate)) {
            sql.append(" AND DEPARTURE_DATE >= :startDate");
            params.addValue("startDate", startDate);
        }
        if (nonNull(endDate)) {
            sql.append(" AND ARRIVAL_DATE <= :endDate");
            params.addValue("endDate", endDate);
        }
        if (nonNull(minPrice)) {
            sql.append(" AND PRICE >= :minPrice");
            params.addValue("minPrice", minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND PRICE <= :maxPrice");
            params.addValue("maxPrice", maxPrice);
        }

        return sql.toString();
    }

    public Long save(TransportEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("type", entity.getType())
                .addValue("provider", entity.getProvider())
                .addValue("departureDate", entity.getDepartureDate())
                .addValue("arrivalDate", entity.getArrivalDate())
                .addValue("origin", entity.getOrigin())
                .addValue("price", entity.getPrice())
                .addValue("capacity", entity.getCapacity())
                .addValue("destinationId", entity.getDestinationId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(TransportEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("type", entity.getType())
                .addValue("provider", entity.getProvider())
                .addValue("departureDate", entity.getDepartureDate())
                .addValue("arrivalDate", entity.getArrivalDate())
                .addValue("origin", entity.getOrigin())
                .addValue("price", entity.getPrice())
                .addValue("capacity", entity.getCapacity())
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
