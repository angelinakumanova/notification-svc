package app.notificationsvc.service;

import app.notificationsvc.model.EmailType;
import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationType;
import app.notificationsvc.repository.NotificationPreferenceRepository;
import app.notificationsvc.repository.NotificationRepository;
import app.notificationsvc.web.dto.OrderCreateEmailRequest;
import app.notificationsvc.web.dto.OrderShippedEmailRequest;
import app.notificationsvc.web.dto.UpsertNotificationPreference;
import app.notificationsvc.web.dto.WelcomeEmailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void givenValidUserId_whenGetByUserId_thenReturnNotificationPreference() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = new NotificationPreference();
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));

        // When
        NotificationPreference fetchedNotificationPreference = notificationService.getByUserId(userId);

        // Then
        assertEquals(notificationPreference, fetchedNotificationPreference);
    }

    @Test
    void givenInvalidUserId_whenGetByUserId_thenThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When && Then
        assertThrows(NullPointerException.class, () -> notificationService.getByUserId(userId));
    }

    @Test
    void givenNonExistentNotificationPreferenceForUser_whenUpsertPreference_thenCreateNotificationPreference() {
        // Given
        UUID userId = UUID.randomUUID();

        UpsertNotificationPreference upsertNotificationPreference = new UpsertNotificationPreference();
        upsertNotificationPreference.setUserId(userId);
        upsertNotificationPreference.setNotificationType(NotificationType.EMAIL);
        upsertNotificationPreference.setNewsletterEnabled(true);
        upsertNotificationPreference.setContactData("test@example.com");

        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);

        // When
        notificationService.upsertPreference(upsertNotificationPreference);

        // Then
        verify(notificationPreferenceRepository, times(1)).save(captor.capture());
        NotificationPreference fetchedNotificationPreference = captor.getValue();

        assertEquals(upsertNotificationPreference.getUserId(), fetchedNotificationPreference.getUserId());
        assertEquals(upsertNotificationPreference.getNotificationType(), fetchedNotificationPreference.getType());
        assertEquals(upsertNotificationPreference.isNewsletterEnabled(), fetchedNotificationPreference.isNewsletterEnabled());
        assertEquals(upsertNotificationPreference.getContactData(), fetchedNotificationPreference.getContactData());
    }

    @Test
    void givenExistentNotificationPreferenceForUser_whenUpsertPreference_thenUpdateNotificationPreference() {
        // Given
        UUID userId = UUID.randomUUID();

        UpsertNotificationPreference upsertNotificationPreference = new UpsertNotificationPreference();
        upsertNotificationPreference.setUserId(userId);
        upsertNotificationPreference.setNotificationType(NotificationType.EMAIL);
        upsertNotificationPreference.setNewsletterEnabled(true);
        upsertNotificationPreference.setContactData("test@example.com");

        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setUserId(userId);
        notificationPreference.setType(NotificationType.EMAIL);
        notificationPreference.setNewsletterEnabled(false);
        notificationPreference.setContactData("testoTEST@example.com");

        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));

        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);

        // When
        notificationService.upsertPreference(upsertNotificationPreference);

        // Then
        verify(notificationPreferenceRepository, times(1)).save(captor.capture());
        NotificationPreference updatedNotificationPreference = captor.getValue();

        assertEquals(upsertNotificationPreference.getUserId(), updatedNotificationPreference.getUserId());
        assertEquals(upsertNotificationPreference.getNotificationType(), updatedNotificationPreference.getType());
        assertEquals(upsertNotificationPreference.isNewsletterEnabled(), updatedNotificationPreference.isNewsletterEnabled());
        assertEquals(upsertNotificationPreference.getContactData(), updatedNotificationPreference.getContactData());
    }

    @Test
    void givenInvalidEmailType_whenSendWelcomeEmail_thenThrowException() {
        // Given
        WelcomeEmailRequest request = new WelcomeEmailRequest();
        request.setEmailType(EmailType.NEWSLETTER);
        request.setUserFirstName("Alice");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendWelcomeEmail(request));
    }

    @Test
    void givenValidWelcomeEmailRequest_whenSendWelcomeEmail_thenProcessTemplateAndSendEmail() {
        // Given
        WelcomeEmailRequest request = new WelcomeEmailRequest();
        UUID userId = UUID.randomUUID();
        request.setUserId(userId);
        request.setEmailType(EmailType.WELCOME);
        request.setUserFirstName("Alice");
        request.setSubject("Welcome to Dripify!");

        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setUserId(userId);
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));

        String expectedBody = "Hello Alice, welcome!";

        when(templateEngine.process(eq(EmailType.WELCOME.getTemplate()), any(Context.class)))
                .thenReturn(expectedBody);

        // When
        notificationService.sendWelcomeEmail(request);

        // Then
        verify(templateEngine, times(1)).process(eq(EmailType.WELCOME.getTemplate()), any(Context.class));
    }

    @Test
    void givenHappyPath_whenSendNewOrderEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setUserId(userId);
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));


        OrderCreateEmailRequest request = new OrderCreateEmailRequest();
        request.setEmailType(EmailType.NEW_ORDER);
        request.setUserId(userId);
        request.setFullName("Gosho Goshev");
        request.setAddress("Ul. Gosheva, Balchik, Bulgaria");
        request.setPhoneNumber("+359 890 090 090");
        request.setCourier("SPEEDY");
        request.setPaymentMethod("CARD");

        String expectedBody = "You have a new order for purchase Gosho Goshev!";

        when(templateEngine.process(eq(EmailType.NEW_ORDER.getTemplate()), any(Context.class)))
                .thenReturn(expectedBody);

        // When
        notificationService.sendNewOrderEmail(request);

        // Then
        verify(templateEngine, times(1)).process(eq(request.getEmailType().getTemplate()), any(Context.class));
    }

    @Test
    void givenInvalidEmailType_whenSendNewOrderEmail_thenThrowException() {
        // Given
        OrderCreateEmailRequest request = new OrderCreateEmailRequest();
        request.setEmailType(EmailType.NEWSLETTER);

        // When && Then
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNewOrderEmail(request));
    }

    @Test
    void givenHappyPath_whenSendShippedOrderEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setUserId(userId);
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));


        OrderShippedEmailRequest request = new OrderShippedEmailRequest();
        request.setEmailType(EmailType.SHIPPED_ORDER);
        request.setUserId(userId);
        request.setOrderId(2L);
        request.setAddress("Ul. Gosheva, Balchik, Bulgaria");
        request.setCourier("SPEEDY");
        request.setPaymentMethod("CARD");
        request.setTotalAmount(BigDecimal.valueOf(333));

        String expectedBody = "Your order has been shipped!";

        when(templateEngine.process(eq(EmailType.SHIPPED_ORDER.getTemplate()), any(Context.class)))
                .thenReturn(expectedBody);

        // When
        notificationService.sendShippedOrderEmail(request);

        // Then
        verify(templateEngine, times(1)).process(eq(request.getEmailType().getTemplate()), any(Context.class));
    }


    @Test
    void givenInvalidEmailType_whenSendShippedOrderEmail_thenThrowException() {
        // Given
        OrderShippedEmailRequest request = new OrderShippedEmailRequest();
        request.setEmailType(EmailType.NEWSLETTER);

        // When && Then
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendShippedOrderEmail(request));
    }

    @Test
    void givenUsersWithNewsletterEnabled_whenSendNewsletter_thenProcessTemplateAndSendEmails() {
        // Given
        NotificationPreference user1 = new NotificationPreference();
        user1.setUserId(UUID.randomUUID());
        when(notificationPreferenceRepository.findByUserId(user1.getUserId())).thenReturn(Optional.of(user1));

        NotificationPreference user2 = new NotificationPreference();
        user2.setUserId(UUID.randomUUID());
        when(notificationPreferenceRepository.findByUserId(user2.getUserId())).thenReturn(Optional.of(user2));

        List<NotificationPreference> newsletterSubscribers = List.of(user1, user2);

        when(notificationPreferenceRepository.findAllByIsNewsletterEnabledTrue()).thenReturn(newsletterSubscribers);
        when(templateEngine.process(eq(EmailType.NEWSLETTER.getTemplate()), any(Context.class)))
                .thenReturn("Mocked newsletter body");

        // When
        notificationService.sendNewsletter();

        // Then
        verify(templateEngine, times(1)).process(eq(EmailType.NEWSLETTER.getTemplate()), any(Context.class));
        verify(notificationPreferenceRepository, times(1)).findAllByIsNewsletterEnabledTrue();
        assertEquals(2, newsletterSubscribers.size());
    }

    @Test
    void givenHappyPath_whenChangeNotificationPreference_thenChangeNotificationPreference() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setNewsletterEnabled(true);
        notificationPreference.setUserId(userId);
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));

        when(notificationPreferenceRepository.save(notificationPreference)).thenReturn(notificationPreference);
        // When
        NotificationPreference savedNotificationPreference = notificationService.changeNotificationPreference(userId, false);

        // Then
        verify(notificationPreferenceRepository, times(1)).save(notificationPreference);
        assertFalse(savedNotificationPreference.isNewsletterEnabled());

    }

}
