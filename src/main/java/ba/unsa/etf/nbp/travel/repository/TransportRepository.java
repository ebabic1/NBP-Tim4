package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.TransportEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class TransportRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_TRANSPORT ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_TRANSPORT WHERE ID = ?";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_TRANSPORT WHERE DESTINATION_ID = ? ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_TRANSPORT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_TRANSPORT (ID, TYPE, PROVIDER, DEPARTURE_DATE, ARRIVAL_DATE, ORIGIN, PRICE, CAPACITY, DESTINATION_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_TRANSPORT
            SET TYPE = ?, PROVIDER = ?, DEPARTURE_DATE = ?, ARRIVAL_DATE = ?,
                ORIGIN = ?, PRICE = ?, CAPACITY = ?, DESTINATION_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_TRANSPORT WHERE ID = ?";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_TRANSPORT";

    private TransportEntity mapRow(ResultSet rs) throws SQLException {
        return TransportEntity.builder()
                .id(rs.getLong("ID"))
                .type(rs.getString("TYPE"))
                .provider(rs.getString("PROVIDER"))
                .departureDate(nonNull(rs.getDate("DEPARTURE_DATE")) ? rs.getDate("DEPARTURE_DATE").toLocalDate() : null)
                .arrivalDate(nonNull(rs.getDate("ARRIVAL_DATE")) ? rs.getDate("ARRIVAL_DATE").toLocalDate() : null)
                .origin(rs.getString("ORIGIN"))
                .price(rs.getBigDecimal("PRICE"))
                .capacity(rs.getInt("CAPACITY"))
                .destinationId(rs.getLong("DESTINATION_ID"))
                .build();
    }

    public List<TransportEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<TransportEntity>();
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

    public Optional<TransportEntity> findById(Long id) {
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

    public List<TransportEntity> findByDestinationId(Long destinationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_DESTINATION_ID)) {
            ps.setLong(1, destinationId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<TransportEntity>();
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

    public List<TransportEntity> search(Long destinationId, String type, String provider,
                                        LocalDate startDate, LocalDate endDate,
                                        BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("*", destinationId, type, provider, startDate, endDate, minPrice, maxPrice, params);
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
                var results = new ArrayList<TransportEntity>();
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

    public long countSearch(Long destinationId, String type, String provider,
                            LocalDate startDate, LocalDate endDate,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("COUNT(*)", destinationId, type, provider, startDate, endDate, minPrice, maxPrice, params);

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

    private String buildSearchQuery(String selectClause, Long destinationId, String type, String provider,
                                    LocalDate startDate, LocalDate endDate,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    List<Object> params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_TRANSPORT WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND DESTINATION_ID = ?");
            params.add(destinationId);
        }
        if (nonNull(type)) {
            sql.append(" AND TYPE = ?");
            params.add(type);
        }
        if (nonNull(provider) && !provider.isBlank()) {
            sql.append(" AND UPPER(PROVIDER) LIKE '%' || UPPER(?) || '%'");
            params.add(provider.trim());
        }
        if (nonNull(startDate)) {
            sql.append(" AND DEPARTURE_DATE >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (nonNull(endDate)) {
            sql.append(" AND ARRIVAL_DATE <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (nonNull(minPrice)) {
            sql.append(" AND PRICE >= ?");
            params.add(minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND PRICE <= ?");
            params.add(maxPrice);
        }

        return sql.toString();
    }

    public Long save(TransportEntity entity) {
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
                ps.setString(2, entity.getType());
                ps.setString(3, entity.getProvider());
                ps.setObject(4, nonNull(entity.getDepartureDate()) ? Date.valueOf(entity.getDepartureDate()) : null);
                ps.setObject(5, nonNull(entity.getArrivalDate()) ? Date.valueOf(entity.getArrivalDate()) : null);
                ps.setString(6, entity.getOrigin());
                ps.setBigDecimal(7, entity.getPrice());
                ps.setInt(8, entity.getCapacity());
                ps.setLong(9, entity.getDestinationId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(TransportEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getType());
            ps.setString(2, entity.getProvider());
            ps.setObject(3, nonNull(entity.getDepartureDate()) ? Date.valueOf(entity.getDepartureDate()) : null);
            ps.setObject(4, nonNull(entity.getArrivalDate()) ? Date.valueOf(entity.getArrivalDate()) : null);
            ps.setString(5, entity.getOrigin());
            ps.setBigDecimal(6, entity.getPrice());
            ps.setInt(7, entity.getCapacity());
            ps.setLong(8, entity.getDestinationId());
            ps.setLong(9, entity.getId());
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
