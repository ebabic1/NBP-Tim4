package ba.unsa.etf.nbp.travel.dto.response;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String email,
        String role
) {
}
