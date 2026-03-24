package ba.unsa.etf.nbp.travel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var path = request.getRequestURI();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        var header = request.getHeader("Authorization");

        if (isNull(header) || !header.startsWith("Bearer ")) {
            sendUnauthorized(response, request.getRequestURI(), "Missing or invalid Authorization header");
            return;
        }

        var token = header.substring(7);

        if (!jwtProvider.isTokenValid(token)) {
            sendUnauthorized(response, request.getRequestURI(), "Invalid or expired JWT token");
            return;
        }

        try {
            var userId = jwtProvider.getUserId(token);
            var username = jwtProvider.getUsername(token);
            var role = jwtProvider.getRole(token);

            AuthContext.set(new AuthContext.AuthenticatedUser(userId, username, role));
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth")
                || path.startsWith("/swagger")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs");
    }

    private void sendUnauthorized(HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        var body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 401,
                "error", "Unauthorized",
                "message", message,
                "path", path
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
