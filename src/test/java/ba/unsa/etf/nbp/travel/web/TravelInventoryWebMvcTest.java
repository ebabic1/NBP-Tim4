package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.AccommodationController;
import ba.unsa.etf.nbp.travel.controller.DestinationController;
import ba.unsa.etf.nbp.travel.controller.DiscountController;
import ba.unsa.etf.nbp.travel.controller.TransportController;
import ba.unsa.etf.nbp.travel.controller.TravelPackageController;
import ba.unsa.etf.nbp.travel.dto.request.AccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.request.DestinationRequest;
import ba.unsa.etf.nbp.travel.dto.request.DiscountRequest;
import ba.unsa.etf.nbp.travel.dto.request.PackageAccommodationRequest;
import ba.unsa.etf.nbp.travel.dto.request.PackageTransportRequest;
import ba.unsa.etf.nbp.travel.dto.request.TransportRequest;
import ba.unsa.etf.nbp.travel.dto.request.TravelPackageRequest;
import ba.unsa.etf.nbp.travel.dto.response.AccommodationResponse;
import ba.unsa.etf.nbp.travel.dto.response.DestinationResponse;
import ba.unsa.etf.nbp.travel.dto.response.DiscountResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.TransportResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageDetailResponse;
import ba.unsa.etf.nbp.travel.dto.response.TravelPackageResponse;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.AccommodationService;
import ba.unsa.etf.nbp.travel.service.DestinationService;
import ba.unsa.etf.nbp.travel.service.DiscountService;
import ba.unsa.etf.nbp.travel.service.TransportService;
import ba.unsa.etf.nbp.travel.service.TravelPackageService;
import ba.unsa.etf.nbp.travel.support.AuthHeader;
import ba.unsa.etf.nbp.travel.support.SecureWebMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SecureWebMvcTest(controllers = {
        DestinationController.class,
        AccommodationController.class,
        TransportController.class,
        TravelPackageController.class,
        DiscountController.class
})
class TravelInventoryWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private DestinationService destinationService;

    @MockitoBean
    private AccommodationService accommodationService;

    @MockitoBean
    private TransportService transportService;

    @MockitoBean
    private TravelPackageService travelPackageService;

    @MockitoBean
    private DiscountService discountService;

    private String agentAuth() {
        return AuthHeader.bearer(jwtProvider, 10L, "agent", "AGENT");
    }

    private String userAuth() {
        return AuthHeader.bearer(jwtProvider, 11L, "user", "USER");
    }

    private String adminAuth() {
        return AuthHeader.bearer(jwtProvider, 12L, "admin", "ADMIN");
    }

    @Test
    void destinations_list_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/destinations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void destinations_list_withUser_returnsOk() throws Exception {
        var page = new PageResponse<DestinationResponse>(List.of(), 0, 10, 0, 0);
        when(destinationService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/destinations")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void destination_create_agent_returnsCreated() throws Exception {
        var req = new DestinationRequest("Sea", "Nice", null, 1L);
        when(destinationService.create(any())).thenReturn(new DestinationResponse(1L, "Sea", "Nice", null, 1L, "City"));

        mockMvc.perform(post("/api/destinations")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void destination_create_user_forbidden() throws Exception {
        var req = new DestinationRequest("Sea", "Nice", null, 1L);

        mockMvc.perform(post("/api/destinations")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void destination_update_and_delete_agent() throws Exception {
        var req = new DestinationRequest("Sea2", null, null, 1L);
        when(destinationService.update(eq(3L), any())).thenReturn(new DestinationResponse(3L, "Sea2", null, null, 1L, "C"));

        mockMvc.perform(put("/api/destinations/3")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/destinations/3")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());
        verify(destinationService).delete(3L);
    }

    @Test
    void accommodations_search_returnsOk() throws Exception {
        var page = new PageResponse<AccommodationResponse>(List.of(), 0, 10, 0, 0);
        when(accommodationService.search(null, null, null, null, null, null, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/accommodations")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void accommodation_create_agent() throws Exception {
        var req = new AccommodationRequest(
                "Hotel", "hotel", 4, null, "h@h.com",
                new BigDecimal("120.00"), 2, 1L, 2L);
        when(accommodationService.create(any())).thenReturn(
                new AccommodationResponse(1L, "Hotel", "hotel", 4, null, "h@h.com",
                        new BigDecimal("120.00"), 2, 1L, 2L, "Dest"));

        mockMvc.perform(post("/api/accommodations")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void transports_search_returnsOk() throws Exception {
        var page = new PageResponse<TransportResponse>(List.of(), 0, 10, 0, 0);
        when(transportService.search(null, null, null, null, null, null, null, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/transports")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void transport_crud_agent() throws Exception {
        var req = new TransportRequest(
                "bus", "Prevoz", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                "A", new BigDecimal("50.00"), 40, 1L);
        when(transportService.create(any())).thenReturn(
                new TransportResponse(9L, "bus", "Prevoz", LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 2), "A", new BigDecimal("50.00"), 40, 1L, "D"));

        mockMvc.perform(post("/api/transports")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        when(transportService.update(eq(9L), any())).thenReturn(
                new TransportResponse(9L, "bus", "Prevoz", LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 2), "A", new BigDecimal("55.00"), 40, 1L, "D"));

        mockMvc.perform(put("/api/transports/9")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/transports/9")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void travelPackages_search_and_detail() throws Exception {
        var listPage = new PageResponse<TravelPackageResponse>(List.of(), 0, 10, 0, 0);
        when(travelPackageService.search(null, null, null, null, null, null, null, 0, 10)).thenReturn(listPage);

        mockMvc.perform(get("/api/travel-packages")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());

        var detail = new TravelPackageDetailResponse(
                1L, "Pkg", null, new BigDecimal("500.00"), 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10), 2L, "Dest",
                List.of(), List.of());
        when(travelPackageService.findById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/travel-packages/1")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void travelPackage_modifyTransportsAndAccommodations_agent() throws Exception {
        var pkgReq = new TravelPackageRequest(
                "Summer", "Desc", new BigDecimal("800.00"), 20,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 14), 1L);
        when(travelPackageService.create(any())).thenReturn(
                new TravelPackageResponse(5L, "Summer", "Desc", new BigDecimal("800.00"), 20,
                        LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 14), 1L, "D"));

        mockMvc.perform(post("/api/travel-packages")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pkgReq)))
                .andExpect(status().isCreated());

        var pt = new PackageTransportRequest(3L, "OUTBOUND", 1);
        mockMvc.perform(post("/api/travel-packages/5/transports")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pt)))
                .andExpect(status().isCreated());
        verify(travelPackageService).addTransport(eq(5L), any());

        mockMvc.perform(delete("/api/travel-packages/5/transports/3")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());

        var pa = new PackageAccommodationRequest(4L, LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 9), 7);
        mockMvc.perform(post("/api/travel-packages/5/accommodations")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pa)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/travel-packages/5/accommodations/4")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void discounts_list_agent() throws Exception {
        when(discountService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/discounts")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void discounts_list_user_forbidden() throws Exception {
        mockMvc.perform(get("/api/discounts")
                        .header("Authorization", userAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void discount_validate_anyAuthenticatedUser() throws Exception {
        when(discountService.validateCode("SAVE10")).thenReturn(
                new DiscountResponse(1L, "SAVE10", new BigDecimal("10"),
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));

        mockMvc.perform(post("/api/discounts/validate")
                        .header("Authorization", userAuth())
                        .param("code", "SAVE10"))
                .andExpect(status().isOk());
    }

    @Test
    void discount_delete_adminOnly() throws Exception {
        mockMvc.perform(delete("/api/discounts/1")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/discounts/1")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isNoContent());
        verify(discountService).delete(1L);
    }

    @Test
    void discount_create_agent() throws Exception {
        var req = new DiscountRequest(
                "NEW10", new BigDecimal("10"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        when(discountService.create(any())).thenReturn(
                new DiscountResponse(2L, "NEW10", new BigDecimal("10"),
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));

        mockMvc.perform(post("/api/discounts")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
