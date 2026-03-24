package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.CountryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CountryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<CountryEntity> ROW_MAPPER = (rs, rowNum) -> CountryEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .code(rs.getString("CODE"))
            .build();

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_COUNTRY ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_COUNTRY WHERE ID = :id";

    public List<CountryEntity> findAll() {
        return jdbcTemplate.query(SELECT_ALL, Map.of(), ROW_MAPPER);
    }

    public Optional<CountryEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }
}
