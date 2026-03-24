package ba.unsa.etf.nbp.travel.security;

public class AuthContext {

    private AuthContext() {
    }

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    public record AuthenticatedUser(Long userId, String username, String role) {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    public static AuthenticatedUser get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
