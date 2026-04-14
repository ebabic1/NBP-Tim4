package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.ReviewEntity;
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
public class ReviewRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_REVIEW WHERE ID = ?";

    private static final String SELECT_BY_BOOKING_ID =
            "SELECT * FROM NBP_REVIEW WHERE BOOKING_ID = ? ORDER BY ID";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_REVIEW ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SELECT_BY_TRAVEL_PACKAGE_ID_PAGED =
            """
            SELECT r.*
            FROM NBP_REVIEW r
            JOIN NBP_BOOKING b ON b.ID = r.BOOKING_ID
            WHERE b.TRAVEL_PACKAGE_ID = ?
            ORDER BY r.ID
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

    private static final String COUNT_BY_TRAVEL_PACKAGE_ID =
            """
            SELECT COUNT(*)
            FROM NBP_REVIEW r
            JOIN NBP_BOOKING b ON b.ID = r.BOOKING_ID
            WHERE b.TRAVEL_PACKAGE_ID = ?
            """;

    private static final String SELECT_BY_ACCOMMODATION_ID_PAGED =
            """
            SELECT r.*
            FROM NBP_REVIEW r
            JOIN NBP_BOOKING b ON b.ID = r.BOOKING_ID
            WHERE b.ACCOMMODATION_ID = ?
            ORDER BY r.ID
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

    private static final String COUNT_BY_ACCOMMODATION_ID =
            """
            SELECT COUNT(*)
            FROM NBP_REVIEW r
            JOIN NBP_BOOKING b ON b.ID = r.BOOKING_ID
            WHERE b.ACCOMMODATION_ID = ?
            """;

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_REVIEW_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_REVIEW (ID, USER_ID, BOOKING_ID, RATING, COMMENT_TEXT, REVIEW_DATE)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_REVIEW WHERE ID = ?";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_REVIEW";

    private static final String EXISTS_BY_USER_ID_AND_BOOKING_ID =
            "SELECT COUNT(*) FROM NBP_REVIEW WHERE USER_ID = ? AND BOOKING_ID = ?";

    private ReviewEntity mapRow(ResultSet rs) throws SQLException {
        return ReviewEntity.builder()
                .id(rs.getLong("ID"))
                .userId(rs.getLong("USER_ID"))
                .bookingId(rs.getLong("BOOKING_ID"))
                .rating(rs.getInt("RATING"))
                .comment(rs.getString("COMMENT_TEXT"))
                .reviewDate(rs.getObject("REVIEW_DATE") != null ? rs.getDate("REVIEW_DATE").toLocalDate() : null)
                .build();
    }

    public Optional<ReviewEntity> findById(Long id) {
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

    public Long save(ReviewEntity entity) {
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
                ps.setLong(2, entity.getUserId());
                ps.setLong(3, entity.getBookingId());
                ps.setInt(4, entity.getRating());
                ps.setString(5, entity.getComment());
                ps.setDate(6, entity.getReviewDate() != null ? Date.valueOf(entity.getReviewDate()) : null);
                ps.executeUpdate();
            }

            return id;
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

    public List<ReviewEntity> findByBookingId(Long bookingId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_BOOKING_ID)) {
            ps.setLong(1, bookingId);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<ReviewEntity>();
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

    public List<ReviewEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<ReviewEntity>();
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

    public List<ReviewEntity> findByTravelPackageId(Long travelPackageId, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_TRAVEL_PACKAGE_ID_PAGED)) {
            ps.setLong(1, travelPackageId);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<ReviewEntity>();
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

    public long countByTravelPackageId(Long travelPackageId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_TRAVEL_PACKAGE_ID)) {
            ps.setLong(1, travelPackageId);
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

    public List<ReviewEntity> findByAccommodationId(Long accommodationId, int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_ACCOMMODATION_ID_PAGED)) {
            ps.setLong(1, accommodationId);
            ps.setInt(2, offset);
            ps.setInt(3, size);
            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<ReviewEntity>();
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

    public long countByAccommodationId(Long accommodationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT_BY_ACCOMMODATION_ID)) {
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

    public boolean existsByUserIdAndBookingId(Long userId, Long bookingId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(EXISTS_BY_USER_ID_AND_BOOKING_ID)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookingId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
