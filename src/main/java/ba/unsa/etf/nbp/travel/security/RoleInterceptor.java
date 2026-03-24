package ba.unsa.etf.nbp.travel.security;

import ba.unsa.etf.nbp.travel.exception.ForbiddenException;
import ba.unsa.etf.nbp.travel.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

import static java.util.Objects.isNull;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        var roleAnnotation = handlerMethod.getMethodAnnotation(Role.class);

        if (isNull(roleAnnotation)) {
            roleAnnotation = handlerMethod.getBeanType().getAnnotation(Role.class);
        }

        if (isNull(roleAnnotation)) {
            return true;
        }

        var currentUser = AuthContext.get();

        if (isNull(currentUser)) {
            throw new UnauthorizedException("Authentication required");
        }

        var allowedRoles = roleAnnotation.value();
        var userRole = currentUser.role();

        if (Arrays.stream(allowedRoles).noneMatch(role -> role.equals(userRole))) {
            throw new ForbiddenException("Insufficient permissions");
        }

        return true;
    }
}
