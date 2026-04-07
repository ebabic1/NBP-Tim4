package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.LogController;
import ba.unsa.etf.nbp.travel.controller.UserController;
import ba.unsa.etf.nbp.travel.dto.request.CreateUserRequest;
import ba.unsa.etf.nbp.travel.dto.request.UpdateUserRequest;
import ba.unsa.etf.nbp.travel.dto.response.LogResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.UserResponse;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.LogService;
import ba.unsa.etf.nbp.travel.service.UserService;
import ba.unsa.etf.nbp.travel.support.AuthHeader;
import ba.unsa.etf.nbp.travel.support.SecureWebMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SecureWebMvcTest(controllers = {UserController.class, LogController.class})
class UserAndLogWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LogService logService;

    private String adminAuth() {
        return AuthHeader.bearer(jwtProvider, 1L, "admin", "ADMIN");
    }

    private String userAuth() {
        return AuthHeader.bearer(jwtProvider, 2L, "regular", "USER");
    }

    @Test
    void users_list_adminOnly() throws Exception {
        var page = new PageResponse<UserResponse>(List.of(), 0, 10, 0, 0);
        when(userService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", userAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void users_me_returnsCurrentUser() throws Exception {
        when(userService.findById(2L)).thenReturn(
                new UserResponse(2L, "A", "B", "a@b.com", "regular", null, null, null, "USER"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("regular"));
    }

    @Test
    void users_crud_admin() throws Exception {
        when(userService.findById(7L)).thenReturn(
                new UserResponse(7L, "A", "B", "a@b.com", "u", null, null, null, "USER"));

        mockMvc.perform(get("/api/users/7")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isOk());

        var create = new CreateUserRequest(
                "F", "L", "new@x.com", "secret12", "newuser", null, null, null, 1L);
        when(userService.create(any())).thenReturn(
                new UserResponse(8L, "F", "L", "new@x.com", "newuser", null, null, null, "USER"));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated());

        var update = new UpdateUserRequest("F2", null, null, null, null, null);
        when(userService.update(eq(8L), any())).thenReturn(
                new UserResponse(8L, "F2", "L", "new@x.com", "newuser", null, null, null, "USER"));

        mockMvc.perform(put("/api/users/8")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        when(userService.assignRole(8L, 2L)).thenReturn(
                new UserResponse(8L, "F2", "L", "new@x.com", "newuser", null, null, null, "AGENT"));

        mockMvc.perform(put("/api/users/8/role")
                        .header("Authorization", adminAuth())
                        .param("roleId", "2"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/8")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isNoContent());
        verify(userService).delete(8L);
    }

    @Test
    void logs_adminOnly() throws Exception {
        var page = new PageResponse<LogResponse>(List.of(), 0, 10, 0, 0);
        when(logService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/logs")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/logs")
                        .header("Authorization", userAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void logs_filterByTableName() throws Exception {
        var page = new PageResponse<LogResponse>(
                List.of(new LogResponse(1L, "INSERT", "USER", LocalDateTime.now(), "NBPT4")),
                0, 10, 1, 1);
        when(logService.findByTableName("USER", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/logs")
                        .header("Authorization", adminAuth())
                        .param("tableName", "USER"))
                .andExpect(status().isOk());
    }

    @Test
    void logs_filterByActionName() throws Exception {
        var page = new PageResponse<LogResponse>(List.of(), 0, 10, 0, 0);
        when(logService.findByActionName("UPDATE", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/logs")
                        .header("Authorization", adminAuth())
                        .param("actionName", "UPDATE"))
                .andExpect(status().isOk());
    }
}
