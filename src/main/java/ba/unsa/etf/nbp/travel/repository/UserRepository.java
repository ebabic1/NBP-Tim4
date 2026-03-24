package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<UserEntity> ROW_MAPPER = (rs, rowNum) -> UserEntity.builder()
            .id(rs.getLong("ID"))
            .firstName(rs.getString("FIRST_NAME"))
            .lastName(rs.getString("LAST_NAME"))
            .email(rs.getString("EMAIL"))
            .password(rs.getString("PASSWORD"))
            .username(rs.getString("USERNAME"))
            .phoneNumber(rs.getString("PHONE_NUMBER"))
            .birthDate(nonNull(rs.getDate("BIRTH_DATE")) ? rs.getDate("BIRTH_DATE").toLocalDate() : null)
            .addressId(rs.getObject("ADDRESS_ID", Long.class))
            .roleId(rs.getLong("ROLE_ID"))
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_USER WHERE ID = :id";

    private static final String SELECT_BY_USERNAME =
            "SELECT * FROM NBP_USER WHERE USERNAME = :username";

    private static final String SELECT_BY_EMAIL =
            "SELECT * FROM NBP_USER WHERE EMAIL = :email";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_USER_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_USER (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, USERNAME, PHONE_NUMBER, BIRTH_DATE, ADDRESS_ID, ROLE_ID)
            VALUES (:id, :firstName, :lastName, :email, :password, :username, :phoneNumber, :birthDate, :addressId, :roleId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_USER
            SET FIRST_NAME = :firstName, LAST_NAME = :lastName, EMAIL = :email, PASSWORD = :password,
                USERNAME = :username, PHONE_NUMBER = :phoneNumber, BIRTH_DATE = :birthDate,
                ADDRESS_ID = :addressId, ROLE_ID = :roleId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_USER WHERE ID = :id";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_USER ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_USER";

    private static final String EXISTS_BY_USERNAME =
            "SELECT COUNT(*) FROM NBP_USER WHERE USERNAME = :username";

    private static final String EXISTS_BY_EMAIL =
            "SELECT COUNT(*) FROM NBP_USER WHERE EMAIL = :email";

    public Optional<UserEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Optional<UserEntity> findByUsername(String username) {
        var results = jdbcTemplate.query(SELECT_BY_USERNAME, Map.of("username", username), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Optional<UserEntity> findByEmail(String email) {
        var results = jdbcTemplate.query(SELECT_BY_EMAIL, Map.of("email", email), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Long save(UserEntity user) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("username", user.getUsername())
                .addValue("phoneNumber", user.getPhoneNumber())
                .addValue("birthDate", user.getBirthDate())
                .addValue("addressId", user.getAddressId())
                .addValue("roleId", user.getRoleId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(UserEntity user) {
        var params = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("username", user.getUsername())
                .addValue("phoneNumber", user.getPhoneNumber())
                .addValue("birthDate", user.getBirthDate())
                .addValue("addressId", user.getAddressId())
                .addValue("roleId", user.getRoleId());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }

    public List<UserEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public boolean existsByUsername(String username) {
        var result = jdbcTemplate.queryForObject(EXISTS_BY_USERNAME, Map.of("username", username), Long.class);
        return nonNull(result) && result > 0;
    }

    public boolean existsByEmail(String email) {
        var result = jdbcTemplate.queryForObject(EXISTS_BY_EMAIL, Map.of("email", email), Long.class);
        return nonNull(result) && result > 0;
    }
}
