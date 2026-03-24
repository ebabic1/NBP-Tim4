package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<RoleEntity> ROW_MAPPER = (rs, rowNum) -> RoleEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ROLE WHERE ID = :id";

    private static final String SELECT_BY_NAME =
            "SELECT * FROM NBP_ROLE WHERE NAME = :name";

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_ROLE ORDER BY ID";

    public Optional<RoleEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Optional<RoleEntity> findByName(String name) {
        var results = jdbcTemplate.query(SELECT_BY_NAME, Map.of("name", name), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<RoleEntity> findAll() {
        return jdbcTemplate.query(SELECT_ALL, Map.of(), ROW_MAPPER);
    }
}
