package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PaymentEntity;
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
public class PaymentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<PaymentEntity> ROW_MAPPER = (rs, rowNum) -> PaymentEntity.builder()
            .id(rs.getLong("ID"))
            .bookingId(rs.getLong("BOOKING_ID"))
            .discountId(rs.getObject("DISCOUNT_ID") != null ? rs.getLong("DISCOUNT_ID") : null)
            .amount(rs.getBigDecimal("AMOUNT"))
            .discountAmount(rs.getBigDecimal("DISCOUNT_AMOUNT"))
            .finalAmount(rs.getBigDecimal("FINAL_AMOUNT"))
            .paymentDate(nonNull(rs.getDate("PAYMENT_DATE")) ? rs.getDate("PAYMENT_DATE").toLocalDate() : null)
            .method(rs.getString("METHOD"))
            .status(rs.getString("STATUS"))
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_PAYMENT WHERE ID = :id";

    private static final String SELECT_BY_BOOKING_ID =
            "SELECT * FROM NBP_PAYMENT WHERE BOOKING_ID = :bookingId";

    private static final String SELECT_BY_USER_ID_PAGED =
            """
            SELECT p.*
            FROM NBP_PAYMENT p
            JOIN NBP_BOOKING b ON b.ID = p.BOOKING_ID
            WHERE b.USER_ID = :userId
            ORDER BY p.ID
            OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
            """;

    private static final String COUNT_BY_USER_ID =
            """
            SELECT COUNT(*)
            FROM NBP_PAYMENT p
            JOIN NBP_BOOKING b ON b.ID = p.BOOKING_ID
            WHERE b.USER_ID = :userId
            """;

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_PAYMENT ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_PAYMENT";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_PAYMENT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_PAYMENT (ID, BOOKING_ID, DISCOUNT_ID, AMOUNT, DISCOUNT_AMOUNT, FINAL_AMOUNT,
                PAYMENT_DATE, METHOD, STATUS)
            VALUES (:id, :bookingId, :discountId, :amount, :discountAmount, :finalAmount,
                :paymentDate, :method, :status)
            """;

    public Optional<PaymentEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Optional<PaymentEntity> findByBookingId(Long bookingId) {
        var results = jdbcTemplate.query(SELECT_BY_BOOKING_ID, Map.of("bookingId", bookingId), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<PaymentEntity> findByUserId(Long userId, int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_BY_USER_ID_PAGED, Map.of("userId", userId, "offset", offset, "size", size), ROW_MAPPER);
    }

    public long countByUserId(Long userId) {
        var result = jdbcTemplate.queryForObject(COUNT_BY_USER_ID, Map.of("userId", userId), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public List<PaymentEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public Long save(PaymentEntity e) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("bookingId", e.getBookingId())
                .addValue("discountId", e.getDiscountId())
                .addValue("amount", e.getAmount())
                .addValue("discountAmount", e.getDiscountAmount())
                .addValue("finalAmount", e.getFinalAmount())
                .addValue("paymentDate", e.getPaymentDate())
                .addValue("method", e.getMethod())
                .addValue("status", e.getStatus());

        jdbcTemplate.update(INSERT, params);
        return id;
    }
}
