package ba.unsa.etf.nbp.travel.dto.response;

import java.time.LocalDateTime;

public record LogResponse(
        Long id,
        String actionName,
        String tableName,
        LocalDateTime dateTime,
        String dbUser
) {
}
