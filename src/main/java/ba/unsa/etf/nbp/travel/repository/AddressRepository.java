package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.AddressEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AddressRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ADDRESS WHERE ID = ?";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_ADDRESS_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_ADDRESS (ID, STREET, ZIP_CODE, CITY_ID)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_ADDRESS
            SET STREET = ?, ZIP_CODE = ?, CITY_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_ADDRESS WHERE ID = ?";

    public Optional<AddressEntity> findById(Long id) {
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

    public Long save(AddressEntity address) {
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
                ps.setString(2, address.getStreet());
                ps.setString(3, address.getZipCode());
                ps.setLong(4, address.getCityId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(AddressEntity address) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, address.getStreet());
            ps.setString(2, address.getZipCode());
            ps.setLong(3, address.getCityId());
            ps.setLong(4, address.getId());
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

    private AddressEntity mapRow(ResultSet rs) throws SQLException {
        return AddressEntity.builder()
                .id(rs.getLong("ID"))
                .street(rs.getString("STREET"))
                .zipCode(rs.getString("ZIP_CODE"))
                .cityId(rs.getLong("CITY_ID"))
                .build();
    }
}
