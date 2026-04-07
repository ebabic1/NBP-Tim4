package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.CityEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CityRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<CityEntity> ROW_MAPPER = (rs, rowNum) -> CityEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .countryId(rs.getLong("COUNTRY_ID"))
            .build();

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_CITY ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_CITY WHERE ID = :id";

    private static final String SELECT_BY_COUNTRY_ID =
            "SELECT * FROM NBP_CITY WHERE COUNTRY_ID = :countryId ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_CITY_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_CITY (ID, NAME, COUNTRY_ID)
            VALUES (:id, :name, :countryId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_CITY
            SET NAME = :name, COUNTRY_ID = :countryId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_CITY WHERE ID = :id";

    public List<CityEntity> findAll() {
        return jdbcTemplate.query(SELECT_ALL, Map.of(), ROW_MAPPER);
    }

    public Optional<CityEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<CityEntity> findByCountryId(Long countryId) {
        return jdbcTemplate.query(SELECT_BY_COUNTRY_ID, Map.of("countryId", countryId), ROW_MAPPER);
    }

    public Long save(CityEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", entity.getName())
                .addValue("countryId", entity.getCountryId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(CityEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("name", entity.getName())
                .addValue("countryId", entity.getCountryId());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }
}
