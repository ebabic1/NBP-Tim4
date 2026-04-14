package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.TravelPackageEntity;
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
public class TravelPackageRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_TRAVEL_PACKAGE ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_TRAVEL_PACKAGE WHERE ID = ?";

    private static final String SELECT_BY_DESTINATION_ID =
            "SELECT * FROM NBP_TRAVEL_PACKAGE WHERE DESTINATION_ID = ? ORDER BY ID";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_TRAVEL_PACKAGE_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_TRAVEL_PACKAGE (ID, NAME, DESCRIPTION, BASE_PRICE, MAX_CAPACITY, START_DATE, END_DATE, DESTINATION_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_TRAVEL_PACKAGE
            SET NAME = ?, DESCRIPTION = ?, BASE_PRICE = ?, MAX_CAPACITY = ?,
                START_DATE = ?, END_DATE = ?, DESTINATION_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_TRAVEL_PACKAGE WHERE ID = ?";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_TRAVEL_PACKAGE";

    private TravelPackageEntity mapRow(ResultSet rs) throws SQLException {
        return TravelPackageEntity.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .basePrice(rs.getBigDecimal("BASE_PRICE"))
                .maxCapacity(rs.getInt("MAX_CAPACITY"))
                .startDate(nonNull(rs.getDate("START_DATE")) ? rs.getDate("START_DATE").toLocalDate() : null)
                .endDate(nonNull(rs.getDate("END_DATE")) ? rs.getDate("END_DATE").toLocalDate() : null)
                .destinationId(rs.getLong("DESTINATION_ID"))
                .build();
    }

    public List<TravelPackageEntity> findAll(int page, int size) {
        var offset = page * size;
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<TravelPackageEntity>();
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

    public Optional<TravelPackageEntity> findById(Long id) {
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

    public List<TravelPackageEntity> findByDestinationId(Long destinationId) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_DESTINATION_ID)) {
            ps.setLong(1, destinationId);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<TravelPackageEntity>();
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

    public List<TravelPackageEntity> search(Long destinationId, Long cityId, Long countryId,
                                            LocalDate startDate, LocalDate endDate,
                                            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("tp.*", destinationId, cityId, countryId,
                startDate, endDate, minPrice, maxPrice, params);

        var offset = page * size;
        sql += " ORDER BY tp.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        params.add(offset);
        params.add(size);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(sql)) {
            for (var i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<TravelPackageEntity>();
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

    public long countSearch(Long destinationId, Long cityId, Long countryId,
                            LocalDate startDate, LocalDate endDate,
                            BigDecimal minPrice, BigDecimal maxPrice) {
        var params = new ArrayList<>();
        var sql = buildSearchQuery("COUNT(*)", destinationId, cityId, countryId,
                startDate, endDate, minPrice, maxPrice, params);

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

    private String buildSearchQuery(String selectClause, Long destinationId, Long cityId, Long countryId,
                                    LocalDate startDate, LocalDate endDate,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    List<Object> params) {
        var sql = new StringBuilder("SELECT " + selectClause + " FROM NBP_TRAVEL_PACKAGE tp");

        if (nonNull(cityId) || nonNull(countryId)) {
            sql.append(" JOIN NBP_DESTINATION d ON tp.DESTINATION_ID = d.ID");
        }
        if (nonNull(countryId)) {
            sql.append(" JOIN NBP_CITY c ON d.CITY_ID = c.ID");
        }

        sql.append(" WHERE 1=1");

        if (nonNull(destinationId)) {
            sql.append(" AND tp.DESTINATION_ID = ?");
            params.add(destinationId);
        }
        if (nonNull(cityId)) {
            sql.append(" AND d.CITY_ID = ?");
            params.add(cityId);
        }
        if (nonNull(countryId)) {
            sql.append(" AND c.COUNTRY_ID = ?");
            params.add(countryId);
        }
        if (nonNull(startDate)) {
            sql.append(" AND tp.START_DATE >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (nonNull(endDate)) {
            sql.append(" AND tp.END_DATE <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (nonNull(minPrice)) {
            sql.append(" AND tp.BASE_PRICE >= ?");
            params.add(minPrice);
        }
        if (nonNull(maxPrice)) {
            sql.append(" AND tp.BASE_PRICE <= ?");
            params.add(maxPrice);
        }

        return sql.toString();
    }

    public Long save(TravelPackageEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var seqPs = conn.prepareStatement(SELECT_NEXT_ID);
             var seqRs = seqPs.executeQuery()) {

            if (!seqRs.next()) {
                throw new RuntimeException("Failed to get next sequence value");
            }
            var id = seqRs.getLong(1);

            try (var ps = conn.prepareStatement(INSERT)) {
                ps.setLong(1, id);
                ps.setString(2, entity.getName());
                ps.setString(3, entity.getDescription());
                ps.setBigDecimal(4, entity.getBasePrice());
                ps.setInt(5, entity.getMaxCapacity());
                ps.setObject(6, nonNull(entity.getStartDate()) ? Date.valueOf(entity.getStartDate()) : null);
                ps.setObject(7, nonNull(entity.getEndDate()) ? Date.valueOf(entity.getEndDate()) : null);
                ps.setLong(8, entity.getDestinationId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(TravelPackageEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setBigDecimal(3, entity.getBasePrice());
            ps.setInt(4, entity.getMaxCapacity());
            ps.setObject(5, nonNull(entity.getStartDate()) ? Date.valueOf(entity.getStartDate()) : null);
            ps.setObject(6, nonNull(entity.getEndDate()) ? Date.valueOf(entity.getEndDate()) : null);
            ps.setLong(7, entity.getDestinationId());
            ps.setLong(8, entity.getId());
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
