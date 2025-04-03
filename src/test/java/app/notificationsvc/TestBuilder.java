package app.notificationsvc;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationType;

import java.util.UUID;

public final class TestBuilder {

    private TestBuilder() {}

    public static NotificationPreference randomNotificationPreference() {
        return NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .contactData("text@example.com")
                .isNewsletterEnabled(true)
                .build();
    }
}
