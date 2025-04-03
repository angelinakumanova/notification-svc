package app.notificationsvc;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationType;
import app.notificationsvc.repository.NotificationPreferenceRepository;
import app.notificationsvc.service.NotificationService;
import app.notificationsvc.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class CreatePreferenceITest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Test
    void createNewNotificationPreference_happyPath() {

        // Given
        UUID userId = UUID.randomUUID();
        UpsertNotificationPreference notificationPreference = new UpsertNotificationPreference();
        notificationPreference.setUserId(userId);
        notificationPreference.setNotificationType(NotificationType.EMAIL);
        notificationPreference.setNewsletterEnabled(true);
        notificationPreference.setContactData("test@example.com");

        // When
        notificationService.upsertPreference(notificationPreference);

        // Then
        List<NotificationPreference> preferences = preferenceRepository.findAll();
        assertThat(preferences).hasSize(1);

        NotificationPreference preference = preferences.get(0);
        assertEquals(userId, preference.getUserId());
    }
}
