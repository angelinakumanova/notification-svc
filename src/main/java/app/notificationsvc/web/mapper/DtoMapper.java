package app.notificationsvc.web.mapper;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.web.dto.NotificationPreferenceResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public NotificationPreferenceResponse fromNotificationPreference(NotificationPreference notificationPreference) {
        return NotificationPreferenceResponse.builder()
                .id(notificationPreference.getId())
                .userId(notificationPreference.getUserId())
                .type(notificationPreference.getType())
                .isNewsletterEnabled(notificationPreference.isNewsletterEnabled())
                .contactData(notificationPreference.getContactData())
                .build();
    }
}
