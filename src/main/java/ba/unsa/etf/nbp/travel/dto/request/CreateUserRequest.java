package ba.unsa.etf.nbp.travel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String username,
        String phoneNumber,
        LocalDate birthDate,
        Long addressId,
        @NotNull Long roleId
) {
}
