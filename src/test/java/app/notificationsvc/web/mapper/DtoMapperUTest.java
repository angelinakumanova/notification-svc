package app.notificationsvc.web.mapper;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationType;
import app.notificationsvc.web.dto.NotificationPreferenceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void givenNotificationPreference_whenFromNotificationPreference_thenReturnNotificationPreferenceResponse() {
        // Given
        NotificationPreference notificationPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .isNewsletterEnabled(true)
                .contactData("test@example.com")
                .type(NotificationType.EMAIL)
                .build();

        // When
        NotificationPreferenceResponse notificationPreferenceResponse = DtoMapper.fromNotificationPreference(notificationPreference);

        assertEquals(notificationPreference.getId(), notificationPreferenceResponse.getId());
        assertEquals(notificationPreference.getUserId(), notificationPreferenceResponse.getUserId());
        assertEquals(notificationPreference.isNewsletterEnabled(), notificationPreferenceResponse.isNewsletterEnabled());
        assertEquals(notificationPreference.getType(), notificationPreferenceResponse.getType());
        assertEquals(notificationPreference.getContactData(), notificationPreferenceResponse.getContactData());

    }
}
