package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.UserEntity;
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

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DataSource dataSource;

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_USER WHERE ID = ?";

    private static final String SELECT_BY_USERNAME =
            "SELECT * FROM NBP_USER WHERE USERNAME = ?";

    private static final String SELECT_BY_EMAIL =
            "SELECT * FROM NBP_USER WHERE EMAIL = ?";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_USER_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_USER (ID, FIRST_NAME, LAST_NAME, EMAIL, "PASSWORD", USERNAME, PHONE_NUMBER, BIRTH_DATE, ADDRESS_ID, ROLE_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_USER
            SET FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, "PASSWORD" = ?,
                USERNAME = ?, PHONE_NUMBER = ?, BIRTH_DATE = ?,
                ADDRESS_ID = ?, ROLE_ID = ?
            WHERE ID = ?
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_USER WHERE ID = ?";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_USER ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_USER";

    private static final String EXISTS_BY_USERNAME =
            "SELECT COUNT(*) FROM NBP_USER WHERE USERNAME = ?";

    private static final String EXISTS_BY_EMAIL =
            "SELECT COUNT(*) FROM NBP_USER WHERE EMAIL = ?";

    public Optional<UserEntity> findById(Long id) {
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

    public Optional<UserEntity> findByUsername(String username) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_USERNAME)) {
            ps.setString(1, username);
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

    public Optional<UserEntity> findByEmail(String email) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
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

    public Long save(UserEntity user) {
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
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getPassword());
                ps.setString(6, user.getUsername());
                ps.setString(7, user.getPhoneNumber());
                ps.setObject(8, nonNull(user.getBirthDate()) ? Date.valueOf(user.getBirthDate()) : null);
                ps.setObject(9, user.getAddressId());
                ps.setLong(10, user.getRoleId());
                ps.executeUpdate();
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void update(UserEntity user) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getUsername());
            ps.setString(6, user.getPhoneNumber());
            ps.setObject(7, nonNull(user.getBirthDate()) ? Date.valueOf(user.getBirthDate()) : null);
            ps.setObject(8, user.getAddressId());
            ps.setLong(9, user.getRoleId());
            ps.setLong(10, user.getId());
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

    public List<UserEntity> findAll(int page, int size) {
        var conn = DataSourceUtils.getConnection(dataSource);
        var offset = page * size;
        try (var ps = conn.prepareStatement(SELECT_ALL_PAGED)) {
            ps.setInt(1, offset);
            ps.setInt(2, size);
            try (var rs = ps.executeQuery()) {
                var results = new ArrayList<UserEntity>();
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

    public long count() {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(COUNT);
             var rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public boolean existsByUsername(String username) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(EXISTS_BY_USERNAME)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public boolean existsByEmail(String email) {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement(EXISTS_BY_EMAIL)) {
            ps.setString(1, email);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private UserEntity mapRow(ResultSet rs) throws SQLException {
        return UserEntity.builder()
                .id(rs.getLong("ID"))
                .firstName(rs.getString("FIRST_NAME"))
                .lastName(rs.getString("LAST_NAME"))
                .email(rs.getString("EMAIL"))
                .password(rs.getString("PASSWORD"))
                .username(rs.getString("USERNAME"))
                .phoneNumber(rs.getString("PHONE_NUMBER"))
                .birthDate(nonNull(rs.getDate("BIRTH_DATE")) ? rs.getDate("BIRTH_DATE").toLocalDate() : null)
                .addressId(rs.getObject("ADDRESS_ID") != null ? rs.getLong("ADDRESS_ID") : null)
                .roleId(rs.getLong("ROLE_ID"))
                .build();
    }
}
