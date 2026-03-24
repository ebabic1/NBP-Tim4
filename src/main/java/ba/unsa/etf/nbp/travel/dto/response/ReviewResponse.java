package ba.unsa.etf.nbp.travel.dto.response;

import java.time.LocalDate;

public record ReviewResponse(
        Long id,
        Long userId,
        String username,
        Long bookingId,
        Integer rating,
        String comment,
        LocalDate reviewDate
) {
}
