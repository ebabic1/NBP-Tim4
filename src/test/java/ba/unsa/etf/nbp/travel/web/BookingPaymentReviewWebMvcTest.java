package ba.unsa.etf.nbp.travel.web;

import ba.unsa.etf.nbp.travel.controller.BookingController;
import ba.unsa.etf.nbp.travel.controller.PaymentController;
import ba.unsa.etf.nbp.travel.controller.ReviewController;
import ba.unsa.etf.nbp.travel.dto.request.BookingRequest;
import ba.unsa.etf.nbp.travel.dto.request.PaymentRequest;
import ba.unsa.etf.nbp.travel.dto.request.ReviewRequest;
import ba.unsa.etf.nbp.travel.dto.response.BookingResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.dto.response.PaymentResponse;
import ba.unsa.etf.nbp.travel.dto.response.ReviewResponse;
import ba.unsa.etf.nbp.travel.security.JwtProvider;
import ba.unsa.etf.nbp.travel.service.BookingService;
import ba.unsa.etf.nbp.travel.service.PaymentService;
import ba.unsa.etf.nbp.travel.service.ReviewService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SecureWebMvcTest(controllers = {BookingController.class, PaymentController.class, ReviewController.class})
class BookingPaymentReviewWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ReviewService reviewService;

    private String userAuth() {
        return AuthHeader.bearer(jwtProvider, 100L, "booker", "USER");
    }

    private String agentAuth() {
        return AuthHeader.bearer(jwtProvider, 101L, "agent", "AGENT");
    }

    private String adminAuth() {
        return AuthHeader.bearer(jwtProvider, 102L, "admin", "ADMIN");
    }

    @Test
    void booking_create_user() throws Exception {
        var req = new BookingRequest("PACKAGE", 1L, null, null);
        when(bookingService.create(eq(100L), any())).thenReturn(
                new BookingResponse(1L, 100L, "PACKAGE", LocalDate.now(), "PENDING",
                        new BigDecimal("100.00"), 1L, null, null));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void booking_create_forbiddenForAgent() throws Exception {
        var req = new BookingRequest("PACKAGE", 1L, null, null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", agentAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void booking_list_agent() throws Exception {
        var page = new PageResponse<BookingResponse>(List.of(), 0, 10, 0, 0);
        when(bookingService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void booking_my_user() throws Exception {
        var page = new PageResponse<BookingResponse>(List.of(), 0, 10, 0, 0);
        when(bookingService.findByUserId(100L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void booking_byId_requiresJwt() throws Exception {
        when(bookingService.findById(5L)).thenReturn(
                new BookingResponse(5L, 100L, "PACKAGE", LocalDate.now(), "PENDING",
                        BigDecimal.ONE, 1L, null, null));

        mockMvc.perform(get("/api/bookings/5"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/bookings/5")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void booking_confirm_agent() throws Exception {
        when(bookingService.confirm(3L)).thenReturn(
                new BookingResponse(3L, 100L, "PACKAGE", LocalDate.now(), "CONFIRMED",
                        BigDecimal.TEN, 1L, null, null));

        mockMvc.perform(put("/api/bookings/3/confirm")
                        .header("Authorization", agentAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void booking_cancel_user() throws Exception {
        when(bookingService.cancel(4L, 100L)).thenReturn(
                new BookingResponse(4L, 100L, "PACKAGE", LocalDate.now(), "CANCELLED",
                        BigDecimal.ONE, 1L, null, null));

        mockMvc.perform(put("/api/bookings/4/cancel")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void payment_create_user() throws Exception {
        var req = new PaymentRequest("CARD", null);
        when(paymentService.create(eq(9L), any(), eq(100L))).thenReturn(
                new PaymentResponse(1L, 9L, null, new BigDecimal("100.00"), BigDecimal.ZERO,
                        new BigDecimal("100.00"), LocalDate.now(), "CARD", "PAID"));

        mockMvc.perform(post("/api/bookings/9/payment")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void payment_getById() throws Exception {
        when(paymentService.findById(11L)).thenReturn(
                new PaymentResponse(11L, 9L, null, BigDecimal.ONE, BigDecimal.ZERO,
                        BigDecimal.ONE, LocalDate.now(), "CARD", "PAID"));

        mockMvc.perform(get("/api/payments/11")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void payment_getByBookingId() throws Exception {
        when(paymentService.findByBookingId(9L)).thenReturn(
                new PaymentResponse(11L, 9L, null, BigDecimal.ONE, BigDecimal.ZERO,
                        BigDecimal.ONE, LocalDate.now(), "CARD", "PAID"));

        mockMvc.perform(get("/api/bookings/9/payment")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void review_create_user() throws Exception {
        var req = new ReviewRequest(5, "Great");
        when(reviewService.create(eq(6L), eq(100L), any())).thenReturn(
                new ReviewResponse(1L, 100L, "booker", 6L, 5, "Great", LocalDate.now()));

        mockMvc.perform(post("/api/bookings/6/review")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void reviews_list_and_get() throws Exception {
        var page = new PageResponse<ReviewResponse>(List.of(), 0, 10, 0, 0);
        when(reviewService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/reviews")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());

        when(reviewService.findById(2L)).thenReturn(
                new ReviewResponse(2L, 100L, "booker", 6L, 4, "ok", LocalDate.now()));

        mockMvc.perform(get("/api/reviews/2")
                        .header("Authorization", userAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void review_delete_userPassesUserIdToService() throws Exception {
        mockMvc.perform(delete("/api/reviews/3")
                        .header("Authorization", userAuth()))
                .andExpect(status().isNoContent());
        verify(reviewService).delete(eq(3L), eq(100L));
    }

    @Test
    void review_delete_adminPassesNullUserId() throws Exception {
        mockMvc.perform(delete("/api/reviews/3")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isNoContent());
        verify(reviewService).delete(eq(3L), isNull());
    }
}
