package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.CityController;
import ba.unsa.etf.nbp.travel.controller.CountryController;
import ba.unsa.etf.nbp.travel.dto.response.CityResponse;
import ba.unsa.etf.nbp.travel.dto.response.CountryResponse;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.CityService;
import ba.unsa.etf.nbp.travel.service.CountryService;
import ba.unsa.etf.nbp.travel.support.AuthHeader;
import ba.unsa.etf.nbp.travel.support.SecureWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SecureWebMvcTest(controllers = {CityController.class, CountryController.class})
class CityCountryWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private CityService cityService;

    @MockitoBean
    private CountryService countryService;

    @Test
    void cities_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cities_withToken_returnsOk() throws Exception {
        when(cityService.findAll()).thenReturn(List.of(new CityResponse(1L, "Sarajevo", 1L, "BiH")));

        mockMvc.perform(get("/api/cities")
                        .header("Authorization", AuthHeader.bearer(jwtProvider, 1L, "u", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sarajevo"));
    }

    @Test
    void cityById_returnsOk() throws Exception {
        when(cityService.findById(5L)).thenReturn(new CityResponse(5L, "Mostar", 1L, "BiH"));

        mockMvc.perform(get("/api/cities/5")
                        .header("Authorization", AuthHeader.bearer(jwtProvider, 1L, "u", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void countries_returnsOk() throws Exception {
        when(countryService.findAll()).thenReturn(List.of(new CountryResponse(1L, "BiH", "BIH")));

        mockMvc.perform(get("/api/countries")
                        .header("Authorization", AuthHeader.bearer(jwtProvider, 1L, "u", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BIH"));
    }

    @Test
    void countryById_returnsOk() throws Exception {
        when(countryService.findById(2L)).thenReturn(new CountryResponse(2L, "Croatia", "HR"));

        mockMvc.perform(get("/api/countries/2")
                        .header("Authorization", AuthHeader.bearer(jwtProvider, 1L, "u", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Croatia"));
    }

    @Test
    void citiesByCountry_returnsOk() throws Exception {
        when(cityService.findByCountryId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/countries/1/cities")
                        .header("Authorization", AuthHeader.bearer(jwtProvider, 1L, "u", "USER")))
                .andExpect(status().isOk());
    }
}
