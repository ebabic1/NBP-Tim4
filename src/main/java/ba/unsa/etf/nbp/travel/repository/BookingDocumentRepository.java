package ba.unsa.etf.nbp.travel.repository;

import ba.unsa.etf.nbp.travel.model.entity.BookingDocumentEntity;
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
public class BookingDocumentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<BookingDocumentEntity> ROW_MAPPER = (rs, rowNum) -> BookingDocumentEntity.builder()
            .id(rs.getLong("ID"))
            .bookingId(rs.getLong("BOOKING_ID"))
            .docType(rs.getString("DOC_TYPE"))
            .filename(rs.getString("FILENAME"))
            .contentType(rs.getString("CONTENT_TYPE"))
            .sizeBytes(rs.getObject("SIZE_BYTES") != null ? rs.getLong("SIZE_BYTES") : null)
            .content(rs.getBytes("CONTENT"))
            .build();

    private static final String SELECT_BY_BOOKING_AND_TYPE =
            "SELECT * FROM NBP_BOOKING_DOCUMENT WHERE BOOKING_ID = :bookingId AND DOC_TYPE = :docType";

    private static final String SELECT_NEXT_ID =
            "SELECT NBP_BOOKING_DOCUMENT_SEQ.NEXTVAL FROM DUAL";

    private static final String INSERT =
            """
            INSERT INTO NBP_BOOKING_DOCUMENT (ID, BOOKING_ID, DOC_TYPE, FILENAME, CONTENT_TYPE, CONTENT, SIZE_BYTES, CREATED_AT)
            VALUES (:id, :bookingId, :docType, :filename, :contentType, :content, :sizeBytes, SYSDATE)
            """;

    private static final String UPDATE =
            """
            UPDATE NBP_BOOKING_DOCUMENT
               SET FILENAME = :filename,
                   CONTENT_TYPE = :contentType,
                   CONTENT = :content,
                   SIZE_BYTES = :sizeBytes,
                   CREATED_AT = SYSDATE
             WHERE BOOKING_ID = :bookingId AND DOC_TYPE = :docType
            """;

    public Optional<BookingDocumentEntity> findByBookingIdAndType(Long bookingId, String docType) {
        var results = jdbcTemplate.query(SELECT_BY_BOOKING_AND_TYPE,
                Map.of("bookingId", bookingId, "docType", docType), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public void upsert(BookingDocumentEntity e) {
        var updated = jdbcTemplate.update(UPDATE, params(e, false));
        if (updated > 0) {
            return;
        }

        var id = jdbcTemplate.queryForObject(SELECT_NEXT_ID, Map.of(), Long.class);
        jdbcTemplate.update(INSERT, params(e, true).addValue("id", id));
    }

    private static MapSqlParameterSource params(BookingDocumentEntity e, boolean includeId) {
        var params = new MapSqlParameterSource()
                .addValue("bookingId", e.getBookingId())
                .addValue("docType", e.getDocType())
                .addValue("filename", e.getFilename())
                .addValue("contentType", e.getContentType())
                .addValue("content", e.getContent())
                .addValue("sizeBytes", e.getSizeBytes());
        if (includeId && nonNull(e.getId())) {
            params.addValue("id", e.getId());
        }
        return params;
    }
}

