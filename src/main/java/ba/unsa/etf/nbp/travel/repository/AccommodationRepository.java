package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.AccommodationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class AccommodationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<AccommodationEntity> ROW_MAPPER = (rs, rowNum) -> AccommodationEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .type(rs.getString("TYPE"))
            .stars(rs.getObject("STARS", Integer.class))
            .phone(rs.getString("PHONE"))
            .email(rs.getString("EMAIL"))
            .pricePerNight(rs.getBigDecimal("PRICE_PER_NIGHT"))
            .capacity(rs.getInt("CAPACITY"))
            .addressId(rs.getLong("ADDRESS_ID"))
            .destinationId(rs.getLong("DESTINATION_ID"))
            .build();

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_ACCOMMODATION ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ACCOMMODATION WHERE ID = :id";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_ACCOMMODATION WHERE DESTINATION_ID = :destinationId ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_ACCOMMODATION_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_ACCOMMODATION (ID, NAME, TYPE, STARS, PHONE, EMAIL, PRICE_PER_NIGHT, CAPACITY, ADDRESS_ID, DESTINATION_ID)
            VALUES (:id, :name, :type, :stars, :phone, :email, :pricePerNight, :capacity, :addressId, :destinationId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_ACCOMMODATION
            SET NAME = :name, TYPE = :type, STARS = :stars, PHONE = :phone, EMAIL = :email,
                PRICE_PER_NIGHT = :pricePerNight, CAPACITY = :capacity, ADDRESS_ID = :addressId,
                DESTINATION_ID = :destinationId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_ACCOMMODATION WHERE ID = :id";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_ACCOMMODATION";

    public List<AccommodationEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public Optional<AccommodationEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<AccommodationEntity> findByDestinationId(Long destinationId) {
        return jdbcTemplate.query(SELECT_BY_DESTINATION_ID, Map.of("destinationId", destinationId), ROW_MAPPER);
    }

    public List<AccommodationEntity> search(Long destinationId, String type, Integer minStars, Integer maxStars,
                                            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("*", destinationId, type, minStars, maxStars, minPrice, maxPrice, params);
        var offset = page * size;
        params.addValue("offset", offset);
        params.addValue("size", size);
        sql += " ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public long countSearch(Long destinationId, String type, Integer minStars, Integer maxStars,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new MapSqlParameterSource();
        var sql = buildSearchQuery("COUNT(*)", destinationId, type, minStars, maxStars, minPrice, maxPrice, params);
        var result = jdbcTemplate.queryForObject(sql, params, Long.class);
        return nonNull(result) ? result : 0L;
    }

    private String buildSearchQuery(String selectClause, Long destinationId, String type, Integer minStars,
                                    Integer maxStars, BigDecimal minPrice, BigDecimal maxPrice,
                                    MapSqlParameterSource params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_ACCOMMODATION WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND DESTINATION_ID = :destinationId");
            params.addValue("destinationId", destinationId);
        }
        if (nonNull(type)) {
            sql.append(" AND TYPE = :type");
            params.addValue("type", type);
        }
        if (nonNull(minStars)) {
            sql.append(" AND STARS >= :minStars");
            params.addValue("minStars", minStars);
        }
        if (nonNull(maxStars)) {
            sql.append(" AND STARS <= :maxStars");
            params.addValue("maxStars", maxStars);
        }
        if (nonNull(minPrice)) {
            sql.append(" AND PRICE_PER_NIGHT >= :minPrice");
            params.addValue("minPrice", minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND PRICE_PER_NIGHT <= :maxPrice");
            params.addValue("maxPrice", maxPrice);
        }

        return sql.toString();
    }

    public Long save(AccommodationEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", entity.getName())
                .addValue("type", entity.getType())
                .addValue("stars", entity.getStars())
                .addValue("phone", entity.getPhone())
                .addValue("email", entity.getEmail())
                .addValue("pricePerNight", entity.getPricePerNight())
                .addValue("capacity", entity.getCapacity())
                .addValue("addressId", entity.getAddressId())
                .addValue("destinationId", entity.getDestinationId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(AccommodationEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("name", entity.getName())
                .addValue("type", entity.getType())
                .addValue("stars", entity.getStars())
                .addValue("phone", entity.getPhone())
                .addValue("email", entity.getEmail())
                .addValue("pricePerNight", entity.getPricePerNight())
                .addValue("capacity", entity.getCapacity())
                .addValue("addressId", entity.getAddressId())
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
