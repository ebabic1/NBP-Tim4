package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.BookingEntity;
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
public class BookingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<BookingEntity> ROW_MAPPER = (rs, rowNum) -> BookingEntity.builder()
            .id(rs.getLong("ID"))
            .userId(rs.getLong("USER_ID"))
            .bookingType(rs.getString("BOOKING_TYPE"))
            .bookingDate(nonNull(rs.getDate("BOOKING_DATE")) ? rs.getDate("BOOKING_DATE").toLocalDate() : null)
            .status(rs.getString("STATUS"))
            .totalPrice(rs.getBigDecimal("TOTAL_PRICE"))
            .travelPackageId(rs.getObject("TRAVEL_PACKAGE_ID") != null ? rs.getLong("TRAVEL_PACKAGE_ID") : null)
            .accommodationId(rs.getObject("ACCOMMODATION_ID") != null ? rs.getLong("ACCOMMODATION_ID") : null)
            .transportId(rs.getObject("TRANSPORT_ID") != null ? rs.getLong("TRANSPORT_ID") : null)
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_BOOKING WHERE ID = :id";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_BOOKING_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_BOOKING (ID, USER_ID, BOOKING_TYPE, BOOKING_DATE, STATUS, TOTAL_PRICE,
                TRAVEL_PACKAGE_ID, ACCOMMODATION_ID, TRANSPORT_ID)
            VALUES (:id, :userId, :bookingType, SYSDATE, :status, :totalPrice,
                :travelPackageId, :accommodationId, :transportId)
            """;

    private static final String UPDATE_STATUS =
            "UPDATE NBP_BOOKING SET STATUS = :status WHERE ID = :id";

    private static final String SELECT_BY_USER_ID_PAGED =
            "SELECT * FROM NBP_BOOKING WHERE USER_ID = :userId ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT_BY_USER_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE USER_ID = :userId";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_BOOKING ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_BOOKING";

    private static final String COUNT_CONFIRMED_BY_TRAVEL_PACKAGE_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE TRAVEL_PACKAGE_ID = :id AND STATUS != 'CANCELLED'";

    private static final String COUNT_CONFIRMED_BY_ACCOMMODATION_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE ACCOMMODATION_ID = :id AND STATUS != 'CANCELLED'";

    private static final String COUNT_CONFIRMED_BY_TRANSPORT_ID =
            "SELECT COUNT(*) FROM NBP_BOOKING WHERE TRANSPORT_ID = :id AND STATUS != 'CANCELLED'";

    public Optional<BookingEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Long save(BookingEntity e) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("userId", e.getUserId())
                .addValue("bookingType", e.getBookingType())
                .addValue("status", e.getStatus())
                .addValue("totalPrice", e.getTotalPrice())
                .addValue("travelPackageId", e.getTravelPackageId())
                .addValue("accommodationId", e.getAccommodationId())
                .addValue("transportId", e.getTransportId());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update(UPDATE_STATUS, Map.of("id", id, "status", status));
    }

    public List<BookingEntity> findByUserId(Long userId, int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_BY_USER_ID_PAGED,
                Map.of("userId", userId, "offset", offset, "size", size), ROW_MAPPER);
    }

    public long countByUserId(Long userId) {
        var result = jdbcTemplate.queryForObject(COUNT_BY_USER_ID, Map.of("userId", userId), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public List<BookingEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public long countConfirmedByTravelPackageId(Long packageId) {
        var result = jdbcTemplate.queryForObject(COUNT_CONFIRMED_BY_TRAVEL_PACKAGE_ID,
                Map.of("id", packageId), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public long countConfirmedByAccommodationId(Long accommodationId) {
        var result = jdbcTemplate.queryForObject(COUNT_CONFIRMED_BY_ACCOMMODATION_ID,
                Map.of("id", accommodationId), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public long countConfirmedByTransportId(Long transportId) {
        var result = jdbcTemplate.queryForObject(COUNT_CONFIRMED_BY_TRANSPORT_ID,
                Map.of("id", transportId), Long.class);
        return nonNull(result) ? result : 0L;
    }
}
