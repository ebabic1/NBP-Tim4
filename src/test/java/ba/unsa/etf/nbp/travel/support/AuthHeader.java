package ba.unsa.etf.nbp.travel.support;

import ba.unsa.etf.nbp.travel.security.JwtProvider;

public final class AuthHeader {

    private AuthHeader() {
    }

    public static String bearer(JwtProvider jwtProvider, long userId, String username, String role) {
        return "Bearer " + jwtProvider.generateToken(userId, username, role);
    }
}
