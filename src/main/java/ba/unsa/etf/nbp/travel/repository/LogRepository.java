package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.LogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<LogEntity> ROW_MAPPER = (rs, rowNum) -> LogEntity.builder()
            .id(rs.getLong("ID"))
            .actionName(rs.getString("ACTION_NAME"))
            .tableName(rs.getString("TABLE_NAME"))
            .dateTime(nonNull(rs.getTimestamp("DATE_TIME")) ? rs.getTimestamp("DATE_TIME").toLocalDateTime() : null)
            .dbUser(rs.getString("DB_USER"))
            .build();

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_LOG ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_LOG";

    private static final String SELECT_BY_TABLE_NAME_PAGED =
            "SELECT * FROM NBP_LOG WHERE TABLE_NAME = :tableName ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT_BY_TABLE_NAME =
            "SELECT COUNT(*) FROM NBP_LOG WHERE TABLE_NAME = :tableName";

    private static final String SELECT_BY_ACTION_NAME_PAGED =
            "SELECT * FROM NBP_LOG WHERE ACTION_NAME = :actionName ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT_BY_ACTION_NAME =
            "SELECT COUNT(*) FROM NBP_LOG WHERE ACTION_NAME = :actionName";

    public List<LogEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public List<LogEntity> findByTableName(String tableName, int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(
                SELECT_BY_TABLE_NAME_PAGED,
                Map.of("tableName", tableName, "offset", offset, "size", size),
                ROW_MAPPER
        );
    }

    public long countByTableName(String tableName) {
        var result = jdbcTemplate.queryForObject(COUNT_BY_TABLE_NAME, Map.of("tableName", tableName), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public List<LogEntity> findByActionName(String actionName, int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(
                SELECT_BY_ACTION_NAME_PAGED,
                Map.of("actionName", actionName, "offset", offset, "size", size),
                ROW_MAPPER
        );
    }

    public long countByActionName(String actionName) {
        var result = jdbcTemplate.queryForObject(COUNT_BY_ACTION_NAME, Map.of("actionName", actionName), Long.class);
        return nonNull(result) ? result : 0L;
    }
}
