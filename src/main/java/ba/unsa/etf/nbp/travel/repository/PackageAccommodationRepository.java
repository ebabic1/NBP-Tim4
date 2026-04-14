package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PackageAccommodationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class PackageAccommodationRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_PACKAGE_ID =
            "SELECT * FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = ? ORDER BY CHECK_IN";

    private static final String INSERT =
            """
            INSERT INTO NBP_PACKAGE_ACCOMMODATION (TRAVEL_PACKAGE_ID, ACCOMMODATION_ID, CHECK_IN, CHECK_OUT, NIGHTS)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String DELETE_BY_PACKAGE_AND_ACCOMMODATION =
            "DELETE FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = ? AND ACCOMMODATION_ID = ?";

    private static final String DELETE_ALL_BY_PACKAGE_ID =
            "DELETE FROM NBP_PACKAGE_ACCOMMODATION WHERE TRAVEL_PACKAGE_ID = ?";

    private PackageAccommodationEntity mapRow(ResultSet rs) throws SQLException {
        return PackageAccommodationEntity.builder()
                .travelPackageId(rs.getLong("TRAVEL_PACKAGE_ID"))
                .accommodationId(rs.getLong("ACCOMMODATION_ID"))
                .checkIn(nonNull(rs.getDate("CHECK_IN")) ? rs.getDate("CHECK_IN").toLocalDate() : null)
                .checkOut(nonNull(rs.getDate("CHECK_OUT")) ? rs.getDate("CHECK_OUT").toLocalDate() : null)
                .nights(rs.getInt("NIGHTS"))
                .build();
    }

    public List<PackageAccommodationEntity> findByPackageId(Long packageId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_PACKAGE_ID)) {
            ps.setLong(1, packageId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<PackageAccommodationEntity>();
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

    public void save(PackageAccommodationEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(INSERT)) {
            ps.setLong(1, entity.getTravelPackageId());
            ps.setLong(2, entity.getAccommodationId());
            ps.setObject(3, nonNull(entity.getCheckIn()) ? Date.valueOf(entity.getCheckIn()) : null);
            ps.setObject(4, nonNull(entity.getCheckOut()) ? Date.valueOf(entity.getCheckOut()) : null);
            ps.setInt(5, entity.getNights());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void deleteByPackageIdAndAccommodationId(Long packageId, Long accommodationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(DELETE_BY_PACKAGE_AND_ACCOMMODATION)) {
            ps.setLong(1, packageId);
            ps.setLong(2, accommodationId);
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
