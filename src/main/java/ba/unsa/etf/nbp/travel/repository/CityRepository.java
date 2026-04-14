package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.CityEntity;
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
public class CityRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_CITY ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_CITY WHERE ID = ?";

    private static final String SELECT_BY_COUNTRY_ID =
            "SELECT * FROM NBP_CITY WHERE COUNTRY_ID = ? ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_CITY_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_CITY (ID, NAME, COUNTRY_ID)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_CITY
            SET NAME = ?, COUNTRY_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_CITY WHERE ID = ?";

    public List<CityEntity> findAll() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL);
             var rs = ps.executeQuery()) {
            var results = new ArrayList<CityEntity>();
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

    public Optional<CityEntity> findById(Long id) {
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

    public List<CityEntity> findByCountryId(Long countryId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_COUNTRY_ID)) {
            ps.setLong(1, countryId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<CityEntity>();
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

    public Long save(CityEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try {
            Long id;
            try (var seqPs = conn.prepareStatement(SELECT_NEXT_ID);
                 var rs = seqPs.executeQuery()) {
                rs.next();
                id = rs.getLong(1);
            }

            try (var ps = conn.prepareStatement(INSERT)) {
                ps.setLong(1, id);
                ps.setString(2, entity.getName());
                ps.setLong(3, entity.getCountryId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(CityEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getName());
            ps.setLong(2, entity.getCountryId());
            ps.setLong(3, entity.getId());
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

    private CityEntity mapRow(ResultSet rs) throws SQLException {
        return CityEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .countryId(rs.getLong("COUNTRY_ID"))
                .build();
    }
}
