package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.AccommodationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class AccommodationRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_ACCOMMODATION ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ACCOMMODATION WHERE ID = ?";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_ACCOMMODATION WHERE DESTINATION_ID = ? ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_ACCOMMODATION_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_ACCOMMODATION (ID, NAME, TYPE, STARS, PHONE, EMAIL, PRICE_PER_NIGHT, CAPACITY, ADDRESS_ID, DESTINATION_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_ACCOMMODATION
            SET NAME = ?, TYPE = ?, STARS = ?, PHONE = ?, EMAIL = ?,
                PRICE_PER_NIGHT = ?, CAPACITY = ?, ADDRESS_ID = ?,
                DESTINATION_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_ACCOMMODATION WHERE ID = ?";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_ACCOMMODATION";

    private AccommodationEntity mapRow(ResultSet rs) throws SQLException {
        return AccommodationEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .type(rs.getString("TYPE"))
                .stars(rs.getObject("STARS") != null ? rs.getInt("STARS") : null)
                .phone(rs.getString("PHONE"))
                .email(rs.getString("EMAIL"))
                .pricePerNight(rs.getBigDecimal("PRICE_PER_NIGHT"))
                .capacity(rs.getInt("CAPACITY"))
                .addressId(rs.getLong("ADDRESS_ID"))
                .destinationId(rs.getLong("DESTINATION_ID"))
                .build();
    }

    public List<AccommodationEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<AccommodationEntity>();
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

    public Optional<AccommodationEntity> findById(Long id) {
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

    public List<AccommodationEntity> findByDestinationId(Long destinationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_DESTINATION_ID)) {
            ps.setLong(1, destinationId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<AccommodationEntity>();
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

    public List<AccommodationEntity> search(Long destinationId, String type, Integer minStars, Integer maxStars,
                                            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("*", destinationId, type, minStars, maxStars, minPrice, maxPrice, params);
        var offset = page * size;
        sql += " ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        params.add(offset);
        params.add(size);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(sql)) {
            for (var i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<AccommodationEntity>();
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

    public long countSearch(Long destinationId, String type, Integer minStars, Integer maxStars,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("COUNT(*)", destinationId, type, minStars, maxStars, minPrice, maxPrice, params);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(sql)) {
            for (var i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    private String buildSearchQuery(String selectClause, Long destinationId, String type, Integer minStars,
                                    Integer maxStars, BigDecimal minPrice, BigDecimal maxPrice,
                                    List<Object> params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_ACCOMMODATION WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND DESTINATION_ID = ?");
            params.add(destinationId);
        }
        if (nonNull(type)) {
            sql.append(" AND TYPE = ?");
            params.add(type);
        }
        if (nonNull(minStars)) {
            sql.append(" AND STARS >= ?");
            params.add(minStars);
        }
        if (nonNull(maxStars)) {
            sql.append(" AND STARS <= ?");
            params.add(maxStars);
        }
        if (nonNull(minPrice)) {
            sql.append(" AND PRICE_PER_NIGHT >= ?");
            params.add(minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND PRICE_PER_NIGHT <= ?");
            params.add(maxPrice);
        }

        return sql.toString();
    }

    public Long save(AccommodationEntity entity) {
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
                ps.setString(3, entity.getType());
                ps.setObject(4, entity.getStars());
                ps.setString(5, entity.getPhone());
                ps.setString(6, entity.getEmail());
                ps.setBigDecimal(7, entity.getPricePerNight());
                ps.setInt(8, entity.getCapacity());
                ps.setLong(9, entity.getAddressId());
                ps.setLong(10, entity.getDestinationId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(AccommodationEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getType());
            ps.setObject(3, entity.getStars());
            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getEmail());
            ps.setBigDecimal(6, entity.getPricePerNight());
            ps.setInt(7, entity.getCapacity());
            ps.setLong(8, entity.getAddressId());
            ps.setLong(9, entity.getDestinationId());
            ps.setLong(10, entity.getId());
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
}
