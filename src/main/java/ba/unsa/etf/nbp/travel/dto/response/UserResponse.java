package ba.unsa.etf.nbp.travel.dto.response;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String username,
        String phoneNumber,
        LocalDate birthDate,
        Long addressId,
        String role
) {
}
