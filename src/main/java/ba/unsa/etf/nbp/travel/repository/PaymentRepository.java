package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
