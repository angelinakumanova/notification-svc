package app.notificationsvc.web.dto;

import app.notificationsvc.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationPreferenceResponse {

    private UUID id;

    private UUID userId;

    private NotificationType type;

    private boolean isNewsletterEnabled;

    private String contactData;
}
