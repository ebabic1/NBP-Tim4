package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.AddressEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AddressRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<AddressEntity> ROW_MAPPER = (rs, rowNum) -> AddressEntity.builder()
            .id(rs.getLong("ID"))
            .street(rs.getString("STREET"))
            .zipCode(rs.getString("ZIP_CODE"))
            .cityId(rs.getLong("CITY_ID"))
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_ADDRESS WHERE ID = :id";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_ADDRESS_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_ADDRESS (ID, STREET, ZIP_CODE, CITY_ID)
            VALUES (:id, :street, :zipCode, :cityId)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_ADDRESS
            SET STREET = :street, ZIP_CODE = :zipCode, CITY_ID = :cityId
            WHERE ID = :id
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_ADDRESS WHERE ID = :id";

    public Optional<AddressEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Long save(AddressEntity address) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("street", address.getStreet())
                .addValue("zipCode", address.getZipCode())
                .addValue("cityId", address.getCityId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void update(AddressEntity address) {
        var params = new MapSqlParameterSource()
                .addValue("id", address.getId())
                .addValue("street", address.getStreet())
                .addValue("zipCode", address.getZipCode())
                .addValue("cityId", address.getCityId());

        jdbcTemplate.update(UPDATE, params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }
}
