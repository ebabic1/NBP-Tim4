package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.LogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_LOG ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_LOG";

    private static final String SELECT_BY_TABLE_NAME_PAGED =
            "SELECT * FROM NBP_LOG WHERE TABLE_NAME = ? ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT_BY_TABLE_NAME =
            "SELECT COUNT(*) FROM NBP_LOG WHERE TABLE_NAME = ?";

    private static final String SELECT_BY_ACTION_NAME_PAGED =
            "SELECT * FROM NBP_LOG WHERE ACTION_NAME = ? ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT_BY_ACTION_NAME =
            "SELECT COUNT(*) FROM NBP_LOG WHERE ACTION_NAME = ?";

    private LogEntity mapRow(ResultSet rs) throws SQLException {
        return LogEntity.builder()
                .id(rs.getLong("ID"))
                .actionName(rs.getString("ACTION_NAME"))
                .tableName(rs.getString("TABLE_NAME"))
                .dateTime(rs.getObject("DATE_TIME") != null ? rs.getTimestamp("DATE_TIME").toLocalDateTime() : null)
                .dbUser(rs.getString("DB_USER"))
                .build();
    }

    public List<LogEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<LogEntity>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public long count() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT);
             var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public List<LogEntity> findByTableName(String tableName, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_TABLE_NAME_PAGED)) {
            ps.setString(1, tableName);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<LogEntity>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public long countByTableName(String tableName) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_TABLE_NAME)) {
            ps.setString(1, tableName);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public List<LogEntity> findByActionName(String actionName, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_ACTION_NAME_PAGED)) {
            ps.setString(1, actionName);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<LogEntity>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public long countByActionName(String actionName) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_ACTION_NAME)) {
            ps.setString(1, actionName);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
