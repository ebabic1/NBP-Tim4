package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        @Email String email,
        String phoneNumber,
        LocalDate birthDate,
        Long addressId
) {
}
