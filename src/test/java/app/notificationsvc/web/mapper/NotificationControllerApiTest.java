package app.notificationsvc.web.mapper;

import app.notificationsvc.model.EmailType;
import app.notificationsvc.model.NotificationType;
import app.notificationsvc.service.NotificationService;
import app.notificationsvc.web.NotificationController;
import app.notificationsvc.web.dto.OrderCreateEmailRequest;
import app.notificationsvc.web.dto.OrderShippedEmailRequest;
import app.notificationsvc.web.dto.UpsertNotificationPreference;
import app.notificationsvc.web.dto.WelcomeEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static app.notificationsvc.TestBuilder.randomNotificationPreference;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestNotificationPreference_happyPath() throws Exception {

        when(notificationService.getByUserId(any())).thenReturn(randomNotificationPreference());
        MockHttpServletRequestBuilder request = get("/api/v1/notifications/preferences").param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("newsletterEnabled").isNotEmpty())
                .andExpect(jsonPath("contactData").isNotEmpty());
    }

    @Test
    void getRequestNotificationPreferenceInvalidUser_shouldReturnInternalServerErrorResponse() throws Exception {


        MockHttpServletRequestBuilder request = get("/api/v1/notifications/preferences").param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postWithBodyToCreatePreference_returns201AndCorrectDtoStructure() throws Exception {

        UpsertNotificationPreference requestDto = new UpsertNotificationPreference();
        requestDto.setUserId(UUID.randomUUID());
        requestDto.setNotificationType(NotificationType.EMAIL);
        requestDto.setContactData("test@example.com");
        requestDto.setNewsletterEnabled(true);

        when(notificationService.upsertPreference(any())).thenReturn(randomNotificationPreference());
        MockHttpServletRequestBuilder request = post("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(requestDto));


        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("newsletterEnabled").isNotEmpty())
                .andExpect(jsonPath("contactData").isNotEmpty());
    }

    @Test
    void putRequestToChangeNotificationPreference_happyPath() throws Exception {
        UUID userId = UUID.randomUUID();
        boolean newsletterEnabled = true;

        when(notificationService.changeNotificationPreference(userId, newsletterEnabled)).thenReturn(randomNotificationPreference());

        MockHttpServletRequestBuilder request = put("/api/v1/notifications/preferences")
                .param("userId", userId.toString())
                .param("enabled", String.valueOf(newsletterEnabled))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("newsletterEnabled").isNotEmpty())
                .andExpect(jsonPath("contactData").isNotEmpty());
    }

    @Test
    void postRequestToWelcomeEmail_happyPath() throws Exception {
        WelcomeEmailRequest welcomeEmailRequest = new WelcomeEmailRequest();
        welcomeEmailRequest.setEmailType(EmailType.WELCOME);
        welcomeEmailRequest.setSubject("Welcome!!");
        welcomeEmailRequest.setUserId(UUID.randomUUID());
        welcomeEmailRequest.setUserFirstName("Angie");

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/emails/welcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(welcomeEmailRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }

    @Test
    void postRequestToOrderConfirmationEmail_happyPath() throws Exception {
        OrderCreateEmailRequest orderCreateEmailRequest = new OrderCreateEmailRequest();
        orderCreateEmailRequest.setUserId(UUID.randomUUID());
        orderCreateEmailRequest.setEmailType(EmailType.ORDER_CONFIRMATION);
        orderCreateEmailRequest.setSubject("Order Confirmation!!");
        orderCreateEmailRequest.setCourier("ECONT");
        orderCreateEmailRequest.setAddress("Address");
        orderCreateEmailRequest.setPaymentMethod("CARD");
        orderCreateEmailRequest.setFullName("Test User");
        orderCreateEmailRequest.setPhoneNumber("890 123 456");

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/emails/order/confirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(orderCreateEmailRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }
    @Test
    void postRequestToNewOrderEmail_happyPath() throws Exception {
        OrderCreateEmailRequest orderCreateEmailRequest = new OrderCreateEmailRequest();

        orderCreateEmailRequest.setUserId(UUID.randomUUID());
        orderCreateEmailRequest.setEmailType(EmailType.NEW_ORDER);
        orderCreateEmailRequest.setSubject("Order Confirmation!!");
        orderCreateEmailRequest.setCourier("ECONT");
        orderCreateEmailRequest.setAddress("Address");
        orderCreateEmailRequest.setPaymentMethod("CARD");
        orderCreateEmailRequest.setFullName("Test User");
        orderCreateEmailRequest.setPhoneNumber("890 123 456");

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/emails/order/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(orderCreateEmailRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }

    @Test
    void postRequestToShippedOrderEmail_happyPath() throws Exception {
        OrderShippedEmailRequest orderShippedEmailRequest = new OrderShippedEmailRequest();

        orderShippedEmailRequest.setOrderId(1L);
        orderShippedEmailRequest.setTotalAmount(BigDecimal.TEN);
        orderShippedEmailRequest.setEmailType(EmailType.SHIPPED_ORDER);
        orderShippedEmailRequest.setSubject("New Order");
        orderShippedEmailRequest.setCourier("ECONT");
        orderShippedEmailRequest.setAddress("Address");
        orderShippedEmailRequest.setPaymentMethod("CARD");
        orderShippedEmailRequest.setUserId(UUID.randomUUID());

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/emails/order/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(orderShippedEmailRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }

    @Test
    void postRequestToNewsletterEmail_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/emails/newsletter");

        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    void getRequestToNonExistentEndpoint_shouldReturnErrorResponse() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/v1/notifications/emails/nonexistent");

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

}
