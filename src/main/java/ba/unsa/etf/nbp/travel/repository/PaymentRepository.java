package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_PAYMENT WHERE ID = ?";

    private static final String SELECT_BY_BOOKING_ID =
            "SELECT * FROM NBP_PAYMENT WHERE BOOKING_ID = ?";

    private static final String SELECT_BY_USER_ID_PAGED =
            """
            SELECT p.*
            FROM NBP_PAYMENT p
            JOIN NBP_BOOKING b ON b.ID = p.BOOKING_ID
            WHERE b.USER_ID = ?
            ORDER BY p.ID
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

    private static final String COUNT_BY_USER_ID =
            """
            SELECT COUNT(*)
            FROM NBP_PAYMENT p
            JOIN NBP_BOOKING b ON b.ID = p.BOOKING_ID
            WHERE b.USER_ID = ?
            """;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_PAYMENT ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_PAYMENT";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_PAYMENT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_PAYMENT (ID, BOOKING_ID, DISCOUNT_ID, AMOUNT, DISCOUNT_AMOUNT, FINAL_AMOUNT,
                PAYMENT_DATE, METHOD, STATUS)
            VALUES (?, ?, ?, ?, ?, ?,
                ?, ?, ?)
            """;

    private PaymentEntity mapRow(ResultSet rs) throws SQLException {
        return PaymentEntity.builder()
                .id(rs.getLong("ID"))
                .bookingId(rs.getLong("BOOKING_ID"))
                .discountId(rs.getObject("DISCOUNT_ID") != null ? rs.getLong("DISCOUNT_ID") : null)
                .amount(rs.getBigDecimal("AMOUNT"))
                .discountAmount(rs.getBigDecimal("DISCOUNT_AMOUNT"))
                .finalAmount(rs.getBigDecimal("FINAL_AMOUNT"))
                .paymentDate(rs.getObject("PAYMENT_DATE") != null ? rs.getDate("PAYMENT_DATE").toLocalDate() : null)
                .method(rs.getString("METHOD"))
                .status(rs.getString("STATUS"))
                .build();
    }

    public Optional<PaymentEntity> findById(Long id) {
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

    public Optional<PaymentEntity> findByBookingId(Long bookingId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_BOOKING_ID)) {
            ps.setLong(1, bookingId);
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

    public List<PaymentEntity> findByUserId(Long userId, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_USER_ID_PAGED)) {
            ps.setLong(1, userId);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<PaymentEntity>();
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

    public List<PaymentEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<PaymentEntity>();
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

    public Long save(PaymentEntity e) {
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
                ps.setLong(2, e.getBookingId());
                ps.setObject(3, e.getDiscountId());
                ps.setBigDecimal(4, e.getAmount());
                ps.setBigDecimal(5, e.getDiscountAmount());
                ps.setBigDecimal(6, e.getFinalAmount());
                ps.setDate(7, e.getPaymentDate() != null ? Date.valueOf(e.getPaymentDate()) : null);
                ps.setString(8, e.getMethod());
                ps.setString(9, e.getStatus());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
