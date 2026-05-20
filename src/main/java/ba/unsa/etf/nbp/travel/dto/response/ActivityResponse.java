package ba.unsa.etf.nbp.travel.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ActivityResponse(
        Long userId,
        String username,
        String action,
        LocalDateTime timestamp
) {
    public String toHumanReadable() {
        var time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        return username + " " + action + " " + time;
    }
}
