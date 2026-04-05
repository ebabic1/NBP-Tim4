package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.AuthController;
import ba.unsa.etf.nbp.travel.dto.request.LoginRequest;
import ba.unsa.etf.nbp.travel.dto.request.RegisterRequest;
import ba.unsa.etf.nbp.travel.dto.response.AuthResponse;
import ba.unsa.etf.nbp.travel.exception.GlobalExceptionHandler;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class, JwtProvider.class})
@TestPropertySource(properties = {
        "app.jwt.secret=nbp-travel-jwt-secret-key-for-development-only-change-in-production",
        "app.jwt.expiration-ms=86400000"
})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_returnsCreated() throws Exception {
        var body = new RegisterRequest("A", "B", "a@b.com", "secret12", "user1", null, LocalDate.of(1990, 1, 1));
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("tok", 1L, "user1", "a@b.com", "USER"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_validationError_returnsBadRequest() throws Exception {
        var body = new RegisterRequest("", "B", "not-an-email", "short", "u", null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returnsOk() throws Exception {
        var body = new LoginRequest("user1", "password123");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("tok", 2L, "user1", "a@b.com", "USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"));
    }
}
