package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ROLE WHERE ID = ?";

    private static final String SELECT_BY_NAME =
            "SELECT * FROM NBP_ROLE WHERE NAME = ?";

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_ROLE ORDER BY ID";

    public Optional<RoleEntity> findById(Long id) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public Optional<RoleEntity> findByName(String name) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public List<RoleEntity> findAll() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL);
             var rs = ps.executeQuery()) {
            var results = new ArrayList<RoleEntity>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private RoleEntity mapRow(ResultSet rs) throws SQLException {
        return RoleEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}
