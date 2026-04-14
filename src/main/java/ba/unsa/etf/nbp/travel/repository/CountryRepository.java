package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.CountryEntity;
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
public class CountryRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_COUNTRY ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_COUNTRY WHERE ID = ?";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_COUNTRY_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_COUNTRY (ID, NAME, CODE)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_COUNTRY
            SET NAME = ?, CODE = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_COUNTRY WHERE ID = ?";

    public List<CountryEntity> findAll() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL);
             var rs = ps.executeQuery()) {
            var results = new ArrayList<CountryEntity>();
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

    public Optional<CountryEntity> findById(Long id) {
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

    public Long save(CountryEntity entity) {
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
                ps.setString(3, entity.getCode());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(CountryEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getCode());
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

    private CountryEntity mapRow(ResultSet rs) throws SQLException {
        return CountryEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .code(rs.getString("CODE"))
                .build();
    }
}
