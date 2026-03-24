package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.DestinationEntity;
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
public class DestinationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<DestinationEntity> ROW_MAPPER = (rs, rowNum) -> DestinationEntity.builder()
            .id(rs.getLong("ID"))
            .name(rs.getString("NAME"))
            .description(rs.getString("DESCRIPTION"))
            .imageUrl(rs.getString("IMAGE_URL"))
            .cityId(rs.getLong("CITY_ID"))
            .build();

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_DESTINATION ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_DESTINATION WHERE ID = :id";

    private static final String SELECT_BY_CITY_ID =
            "SELECT * FROM NBP_DESTINATION WHERE CITY_ID = :cityId ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_DESTINATION_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_DESTINATION (ID, NAME, DESCRIPTION, IMAGE_URL, CITY_ID)
            VALUES (:id, :name, :description, :imageUrl, :cityId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_DESTINATION
            SET NAME = :name, DESCRIPTION = :description, IMAGE_URL = :imageUrl, CITY_ID = :cityId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_DESTINATION WHERE ID = :id";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_DESTINATION";

    private static final String COUNT_BY_CITY_ID =
            "SELECT COUNT(*) FROM NBP_DESTINATION WHERE CITY_ID = :cityId";

    public List<DestinationEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public Optional<DestinationEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<DestinationEntity> findByCityId(Long cityId) {
        return jdbcTemplate.query(SELECT_BY_CITY_ID, Map.of("cityId", cityId), ROW_MAPPER);
    }

    public Long save(DestinationEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("imageUrl", entity.getImageUrl())
                .addValue("cityId", entity.getCityId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(DestinationEntity entity) {
        var params = new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("imageUrl", entity.getImageUrl())
                .addValue("cityId", entity.getCityId());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public long countByCityId(Long cityId) {
        var result = jdbcTemplate.queryForObject(COUNT_BY_CITY_ID, Map.of("cityId", cityId), Long.class);
        return nonNull(result) ? result : 0L;
    }
}
