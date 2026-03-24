package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.DiscountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class DiscountRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<DiscountEntity> ROW_MAPPER = (rs, rowNum) -> DiscountEntity.builder()
            .id(rs.getLong("ID"))
            .code(rs.getString("CODE"))
            .percentage(rs.getBigDecimal("PERCENTAGE"))
            .validFrom(nonNull(rs.getDate("VALID_FROM")) ? rs.getDate("VALID_FROM").toLocalDate() : null)
            .validTo(nonNull(rs.getDate("VALID_TO")) ? rs.getDate("VALID_TO").toLocalDate() : null)
            .description(rs.getString("DESCRIPTION"))
            .build();

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_DISCOUNT ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_DISCOUNT WHERE ID = :id";

    private static final String SELECT_BY_CODE =
            "SELECT * FROM NBP_DISCOUNT WHERE CODE = :code";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_DISCOUNT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_DISCOUNT (ID, CODE, PERCENTAGE, VALID_FROM, VALID_TO, DESCRIPTION)
            VALUES (:id, :code, :percentage, :validFrom, :validTo, :description)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_DISCOUNT
            SET CODE = :code, PERCENTAGE = :percentage, VALID_FROM = :validFrom, VALID_TO = :validTo,
                DESCRIPTION = :description
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_DISCOUNT WHERE ID = :id";

    public List<DiscountEntity> findAll() {
        return jdbcTemplate.query(SELECT_ALL, Map.of(), ROW_MAPPER);
    }

    public Optional<DiscountEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Optional<DiscountEntity> findByCode(String code) {
        var results = jdbcTemplate.query(SELECT_BY_CODE, Map.of("code", code), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Long save(DiscountEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", entity.getCode())
                .addValue("percentage", entity.getPercentage())
                .addValue("validFrom", entity.getValidFrom())
                .addValue("validTo", entity.getValidTo())
                .addValue("description", entity.getDescription());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(DiscountEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("code", entity.getCode())
                .addValue("percentage", entity.getPercentage())
                .addValue("validFrom", entity.getValidFrom())
                .addValue("validTo", entity.getValidTo())
                .addValue("description", entity.getDescription());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }
}
