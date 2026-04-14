package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.DestinationEntity;
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
public class DestinationRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_DESTINATION ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_DESTINATION WHERE ID = ?";

    private static final String SELECT_BY_CITY_ID =
            "SELECT * FROM NBP_DESTINATION WHERE CITY_ID = ? ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_DESTINATION_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_DESTINATION (ID, NAME, DESCRIPTION, IMAGE_URL, CITY_ID)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_DESTINATION
            SET NAME = ?, DESCRIPTION = ?, IMAGE_URL = ?, CITY_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_DESTINATION WHERE ID = ?";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_DESTINATION";

    private static final String COUNT_BY_CITY_ID =
            "SELECT COUNT(*) FROM NBP_DESTINATION WHERE CITY_ID = ?";

    private DestinationEntity mapRow(ResultSet rs) throws SQLException {
        return DestinationEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .imageUrl(rs.getString("IMAGE_URL"))
                .cityId(rs.getLong("CITY_ID"))
                .build();
    }

    public List<DestinationEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<DestinationEntity>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public Optional<DestinationEntity> findById(Long id) {
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

    public List<DestinationEntity> findByCityId(Long cityId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_CITY_ID)) {
            ps.setLong(1, cityId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<DestinationEntity>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public Long save(DestinationEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try {
            Long id;
            try (var ps = conn.prepareStatement(SELECT_NEXT_ID);
                 var rs = ps.executeQuery()) {
                rs.next();
                id = rs.getLong(1);
            }

            try (var ps = conn.prepareStatement(INSERT)) {
                ps.setLong(1, id);
                ps.setString(2, entity.getName());
                ps.setString(3, entity.getDescription());
                ps.setString(4, entity.getImageUrl());
                ps.setLong(5, entity.getCityId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(DestinationEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setString(3, entity.getImageUrl());
            ps.setLong(4, entity.getCityId());
            ps.setLong(5, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void deleteById(Long id) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(DELETE_BY_ID)) {
            ps.setLong(1, id);
            ps.executeUpdate();
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

    public long countByCityId(Long cityId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_CITY_ID)) {
            ps.setLong(1, cityId);
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
