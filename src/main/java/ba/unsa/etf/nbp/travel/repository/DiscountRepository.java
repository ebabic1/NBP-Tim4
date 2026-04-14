package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.DiscountEntity;
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
public class DiscountRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL =
            "SELECT * FROM NBP_DISCOUNT ORDER BY ID";

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_DISCOUNT WHERE ID = ?";

    private static final String SELECT_BY_CODE =
            "SELECT * FROM NBP_DISCOUNT WHERE CODE = ?";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_DISCOUNT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_DISCOUNT (ID, CODE, PERCENTAGE, VALID_FROM, VALID_TO, DESCRIPTION)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_DISCOUNT
            SET CODE = ?, PERCENTAGE = ?, VALID_FROM = ?, VALID_TO = ?,
                DESCRIPTION = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_DISCOUNT WHERE ID = ?";

    private DiscountEntity mapRow(ResultSet rs) throws SQLException {
        return DiscountEntity.builder()
                .id(rs.getLong("ID"))
                .code(rs.getString("CODE"))
                .percentage(rs.getBigDecimal("PERCENTAGE"))
                .validFrom(rs.getObject("VALID_FROM") != null ? rs.getDate("VALID_FROM").toLocalDate() : null)
                .validTo(rs.getObject("VALID_TO") != null ? rs.getDate("VALID_TO").toLocalDate() : null)
                .description(rs.getString("DESCRIPTION"))
                .build();
    }

    public List<DiscountEntity> findAll() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_ALL);
             var rs = ps.executeQuery()) {
            var list = new ArrayList<DiscountEntity>();
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

    public Optional<DiscountEntity> findById(Long id) {
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

    public Optional<DiscountEntity> findByCode(String code) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_CODE)) {
            ps.setString(1, code);
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

    public Long save(DiscountEntity entity) {
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
                ps.setString(2, entity.getCode());
                ps.setBigDecimal(3, entity.getPercentage());
                ps.setDate(4, entity.getValidFrom() != null ? Date.valueOf(entity.getValidFrom()) : null);
                ps.setDate(5, entity.getValidTo() != null ? Date.valueOf(entity.getValidTo()) : null);
                ps.setString(6, entity.getDescription());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(DiscountEntity entity) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, entity.getCode());
            ps.setBigDecimal(2, entity.getPercentage());
            ps.setDate(3, entity.getValidFrom() != null ? Date.valueOf(entity.getValidFrom()) : null);
            ps.setDate(4, entity.getValidTo() != null ? Date.valueOf(entity.getValidTo()) : null);
            ps.setString(5, entity.getDescription());
            ps.setLong(6, entity.getId());
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
}
