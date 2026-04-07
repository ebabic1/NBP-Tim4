package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.CityController;
import ba.unsa.etf.nbp.travel.controller.CountryController;
import ba.unsa.etf.nbp.travel.dto.request.CityRequest;
import ba.unsa.etf.nbp.travel.dto.request.CountryRequest;
import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.CityService;
import ba.unsa.etf.nbp.travel.service.CountryService;
import ba.unsa.etf.nbp.travel.support.AuthHeader;
import ba.unsa.etf.nbp.travel.support.SecureWebMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SecureWebMvcTest(controllers = {CityController.class, CountryController.class})
class CityCountryWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CityService cityService;

    @MockitoBean
    private CountryService countryService;

    private String userAuth() {
        return AuthHeader.bearer(jwtProvider, 1L, "u", "USER");
    }

    private String agentAuth() {
        return AuthHeader.bearer(jwtProvider, 2L, "a", "AGENT");
    }

    @Test
    void cities_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cities_withToken_returnsOk() throws Exception {
        when(cityService.findAll()).thenReturn(List.of(new CityResponse(1L, "Sarajevo", 1L, "BiH")));

        mockMvc.perform(get("/api/cities")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sarajevo"));
    }

    @Test
    void cityById_returnsOk() throws Exception {
        when(cityService.findById(5L)).thenReturn(new CityResponse(5L, "Mostar", 1L, "BiH"));

        mockMvc.perform(get("/api/cities/5")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void countries_returnsOk() throws Exception {
        when(countryService.findAll()).thenReturn(List.of(new CountryResponse(1L, "BiH", "BIH")));

        mockMvc.perform(get("/api/countries")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BIH"));
    }

    @Test
    void countryById_returnsOk() throws Exception {
        when(countryService.findById(2L)).thenReturn(new CountryResponse(2L, "Croatia", "HR"));

        mockMvc.perform(get("/api/countries/2")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Croatia"));
    }

    @Test
    void citiesByCountry_returnsOk() throws Exception {
        when(cityService.findByCountryId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/countries/1/cities")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void country_crud_agent() throws Exception {
        var req = new CountryRequest("Italy", "IT");
        when(countryService.create(any())).thenReturn(new CountryResponse(10L, "Italy", "IT"));

        mockMvc.perform(post("/api/countries")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        when(countryService.update(eq(10L), any())).thenReturn(new CountryResponse(10L, "Italia", "IT"));

        mockMvc.perform(put("/api/countries/10")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CountryRequest("Italia", "IT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Italia"));

        mockMvc.perform(delete("/api/countries/10")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void city_crud_agent() throws Exception {
        var req = new CityRequest("Rome", 10L);
        when(cityService.create(any())).thenReturn(new CityResponse(20L, "Rome", 10L, "Italy"));

        mockMvc.perform(post("/api/cities")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        when(cityService.update(eq(20L), any())).thenReturn(new CityResponse(20L, "Roma", 10L, "Italy"));

        mockMvc.perform(put("/api/cities/20")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CityRequest("Roma", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roma"));

        mockMvc.perform(delete("/api/cities/20")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void country_create_user_forbidden() throws Exception {
        var req = new CountryRequest("Italy", "IT");

        mockMvc.perform(post("/api/countries")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void city_create_user_forbidden() throws Exception {
        var req = new CityRequest("Rome", 10L);

        mockMvc.perform(post("/api/cities")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
