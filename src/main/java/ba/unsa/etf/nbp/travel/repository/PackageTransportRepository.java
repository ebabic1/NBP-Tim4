package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PackageTransportEntity;
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
public class PackageTransportRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_PACKAGE_ID =
            "SELECT * FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = ? ORDER BY SEQUENCE_ORDER";

    private static final String INSERT =
            """
            INSERT INTO NBP_PACKAGE_TRANSPORT (TRAVEL_PACKAGE_ID, TRANSPORT_ID, TRANSPORT_ROLE, SEQUENCE_ORDER)
            VALUES (?, ?, ?, ?)
            """;

    private static final String DELETE_BY_PACKAGE_AND_TRANSPORT =
            "DELETE FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = ? AND TRANSPORT_ID = ?";

    private static final String DELETE_ALL_BY_PACKAGE_ID =
            "DELETE FROM NBP_PACKAGE_TRANSPORT WHERE TRAVEL_PACKAGE_ID = ?";

    private PackageTransportEntity mapRow(ResultSet rs) throws SQLException {
        return PackageTransportEntity.builder()
                .travelPackageId(rs.getLong("TRAVEL_PACKAGE_ID"))
                .transportId(rs.getLong("TRANSPORT_ID"))
                .transportRole(rs.getString("TRANSPORT_ROLE"))
                .sequenceOrder(rs.getInt("SEQUENCE_ORDER"))
                .build();
    }

    public List<PackageTransportEntity> findByPackageId(Long packageId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_PACKAGE_ID)) {
            ps.setLong(1, packageId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<PackageTransportEntity>();
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

    public void save(PackageTransportEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(INSERT)) {
            ps.setLong(1, entity.getTravelPackageId());
            ps.setLong(2, entity.getTransportId());
            ps.setString(3, entity.getTransportRole());
            ps.setInt(4, entity.getSequenceOrder());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void deleteByPackageIdAndTransportId(Long packageId, Long transportId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(DELETE_BY_PACKAGE_AND_TRANSPORT)) {
            ps.setLong(1, packageId);
            ps.setLong(2, transportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void deleteAllByPackageId(Long packageId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(DELETE_ALL_BY_PACKAGE_ID)) {
            ps.setLong(1, packageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
