package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.BookingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import ba.unsa.etf.nbp.travel.dto.response.ImportResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_BOOKING WHERE ID = ?";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_BOOKING_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_BOOKING (ID, USER_ID, BOOKING_TYPE, BOOKING_DATE, STATUS, TOTAL_PRICE,
                TRAVEL_PACKAGE_ID, ACCOMMODATION_ID, TRANSPORT_ID)
            VALUES (?, ?, ?, SYSDATE, ?, ?,
                ?, ?, ?)
            """;

    private static final String UPDATE_STATUS =
            "UPDATE NBP_BOOKING SET STATUS = ? WHERE ID = ?";

    private static final String CALL_CREATE_BOOKING =
            "{ call NBP_BOOKING_PKG.CREATE_BOOKING(?, ?, ?, ?, ?, ?, ?) }";

    private static final String CALL_CONFIRM_BOOKING =
            "{ call NBP_BOOKING_PKG.CONFIRM_BOOKING(?) }";

    private static final String CALL_CANCEL_BOOKING =
            "{ call NBP_BOOKING_PKG.CANCEL_BOOKING(?) }";

    private static final String SELECT_BY_USER_ID_PAGED =
            "SELECT * FROM NBP_BOOKING WHERE USER_ID = ? ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT_BY_USER_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE USER_ID = ?";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_BOOKING ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_BOOKING";

    private static final String COUNT_CONFIRMED_BY_TRAVEL_PACKAGE_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE TRAVEL_PACKAGE_ID = ? AND STATUS != 'CANCELLED'";

    private static final String COUNT_CONFIRMED_BY_ACCOMMODATION_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE ACCOMMODATION_ID = ? AND STATUS != 'CANCELLED'";

    private static final String COUNT_CONFIRMED_BY_TRANSPORT_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE TRANSPORT_ID = ? AND STATUS != 'CANCELLED'";

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_BOOKING ORDER BY ID";

    private static final String CALL_IMPORT_BOOKINGS_XML =
            "{ call NBP_BOOKING_PKG.IMPORT_BOOKINGS_XML(?, ?, ?) }";

    private BookingEntity mapRow(ResultSet rs) throws SQLException {
        return BookingEntity.builder()
                .id(rs.getLong("ID"))
                .userId(rs.getLong("USER_ID"))
                .bookingType(rs.getString("BOOKING_TYPE"))
                .bookingDate(rs.getObject("BOOKING_DATE") != null ? rs.getDate("BOOKING_DATE").toLocalDate() : null)
                .status(rs.getString("STATUS"))
                .totalPrice(rs.getBigDecimal("TOTAL_PRICE"))
                .travelPackageId(rs.getObject("TRAVEL_PACKAGE_ID") != null ? rs.getLong("TRAVEL_PACKAGE_ID") : null)
                .accommodationId(rs.getObject("ACCOMMODATION_ID") != null ? rs.getLong("ACCOMMODATION_ID") : null)
                .transportId(rs.getObject("TRANSPORT_ID") != null ? rs.getLong("TRANSPORT_ID") : null)
                .build();
    }

    public Optional<BookingEntity> findById(Long id) {
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

    public Long save(BookingEntity e) {
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
                ps.setLong(2, e.getUserId());
                ps.setString(3, e.getBookingType());
                ps.setString(4, e.getStatus());
                ps.setBigDecimal(5, e.getTotalPrice());
                ps.setObject(6, e.getTravelPackageId());
                ps.setObject(7, e.getAccommodationId());
                ps.setObject(8, e.getTransportId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public Long createWithPackage(BookingEntity e) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var cs = conn.prepareCall(CALL_CREATE_BOOKING)) {
            cs.setLong(1, e.getUserId());
            cs.setString(2, e.getBookingType());
            cs.setBigDecimal(3, e.getTotalPrice());
            cs.setObject(4, e.getTravelPackageId());
            cs.setObject(5, e.getAccommodationId());
            cs.setObject(6, e.getTransportId());
            cs.registerOutParameter(7, Types.NUMERIC);
            cs.execute();
            return cs.getLong(7);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void confirmWithPackage(Long id) {
        callStatusProcedure(CALL_CONFIRM_BOOKING, id);
    }

    public void cancelWithPackage(Long id) {
        callStatusProcedure(CALL_CANCEL_BOOKING, id);
    }

    private void callStatusProcedure(String sql, Long id) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var cs = conn.prepareCall(sql)) {
            cs.setLong(1, id);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void updateStatus(Long id, String status) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public List<BookingEntity> findByUserId(Long userId, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_USER_ID_PAGED)) {
            ps.setLong(1, userId);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<BookingEntity>();
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

    public long countByUserId(Long userId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_USER_ID)) {
            ps.setLong(1, userId);
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

    public List<BookingEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<BookingEntity>();
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

    public List<BookingEntity> findAll() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL);
             var rs = ps.executeQuery()) {
            var list = new ArrayList<BookingEntity>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public ImportResult importBookingsXml(String xml) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var cs = conn.prepareCall(CALL_IMPORT_BOOKINGS_XML)) {
            var clob = conn.createClob();
            clob.setString(1, xml);
            cs.setClob(1, clob);
            cs.registerOutParameter(2, Types.NUMERIC);
            cs.registerOutParameter(3, Types.NUMERIC);
            cs.execute();
            return new ImportResult(cs.getLong(2), cs.getLong(3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public long countConfirmedByTravelPackageId(Long packageId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_CONFIRMED_BY_TRAVEL_PACKAGE_ID)) {
            ps.setLong(1, packageId);
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

    public long countConfirmedByAccommodationId(Long accommodationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_CONFIRMED_BY_ACCOMMODATION_ID)) {
            ps.setLong(1, accommodationId);
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

    public long countConfirmedByTransportId(Long transportId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_CONFIRMED_BY_TRANSPORT_ID)) {
            ps.setLong(1, transportId);
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
