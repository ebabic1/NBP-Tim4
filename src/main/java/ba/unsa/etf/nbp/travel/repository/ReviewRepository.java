package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.ReviewEntity;
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
public class ReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<ReviewEntity> ROW_MAPPER = (rs, rowNum) -> ReviewEntity.builder()
            .id(rs.getLong("ID"))
            .userId(rs.getLong("USER_ID"))
            .bookingId(rs.getLong("BOOKING_ID"))
            .rating(rs.getInt("RATING"))
            .comment(rs.getString("COMMENT_TEXT"))
            .reviewDate(nonNull(rs.getDate("REVIEW_DATE")) ? rs.getDate("REVIEW_DATE").toLocalDate() : null)
            .build();

    private static final String SELECT_BY_ID =
            "SELECT * FROM NBP_REVIEW WHERE ID = :id";

    private static final String SELECT_BY_BOOKING_ID =
            "SELECT * FROM NBP_REVIEW WHERE BOOKING_ID = :bookingId ORDER BY ID";

    private static final String SELECT_ALL_PAGED =
            "SELECT * FROM NBP_REVIEW ORDER BY ID OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_REVIEW_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_REVIEW (ID, USER_ID, BOOKING_ID, RATING, COMMENT_TEXT, REVIEW_DATE)
            VALUES (:id, :userId, :bookingId, :rating, :comment, :reviewDate)
            """;

    private static final String DELETE_BY_ID =
            "DELETE FROM NBP_REVIEW WHERE ID = :id";

    private static final String COUNT =
            "SELECT COUNT(*) FROM NBP_REVIEW";

    private static final String EXISTS_BY_USER_ID_AND_BOOKING_ID =
            "SELECT COUNT(*) FROM NBP_REVIEW WHERE USER_ID = :userId AND BOOKING_ID = :bookingId";

    public Optional<ReviewEntity> findById(Long id) {
        var results = jdbcTemplate.query(SELECT_BY_ID, Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public Long save(ReviewEntity entity) {
        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("userId", entity.getUserId())
                .addValue("bookingId", entity.getBookingId())
                .addValue("rating", entity.getRating())
                .addValue("comment", entity.getComment())
                .addValue("reviewDate", entity.getReviewDate());

        jdbcTemplate.update(INSERT, params);
        return id;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID, Map.of("id", id));
    }

    public List<ReviewEntity> findByBookingId(Long bookingId) {
        return jdbcTemplate.query(SELECT_BY_BOOKING_ID, Map.of("bookingId", bookingId), ROW_MAPPER);
    }

    public List<ReviewEntity> findAll(int page, int size) {
        var offset = page * size;
        return jdbcTemplate.query(SELECT_ALL_PAGED, Map.of("offset", offset, "size", size), ROW_MAPPER);
    }

    public long count() {
        var result = jdbcTemplate.queryForObject(COUNT, Map.of(), Long.class);
        return nonNull(result) ? result : 0L;
    }

    public boolean existsByUserIdAndBookingId(Long userId, Long bookingId) {
        var result = jdbcTemplate.queryForObject(
                EXISTS_BY_USER_ID_AND_BOOKING_ID,
                Map.of("userId", userId, "bookingId", bookingId),
                Long.class
        );
        return nonNull(result) && result > 0;
    }
}
