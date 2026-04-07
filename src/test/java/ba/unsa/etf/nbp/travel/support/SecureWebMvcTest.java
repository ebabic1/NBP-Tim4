package ba.unsa.etf.nbp.travel.support;

import ba.unsa.etf.nbp.travel.exception.GlobalExceptionHandler;
import ba.unsa.etf.nbp.travel.security.JwtFilter;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.security.RoleInterceptor;
import ba.unsa.etf.nbp.travel.security.SecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WebMvcTest
@Import({SecurityConfig.class, JwtFilter.class, RoleInterceptor.class, JwtProvider.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt.secret=nbp-travel-jwt-secret-key-for-development-only-change-in-production",
        "app.jwt.expiration-ms=86400000"
})
public @interface SecureWebMvcTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
