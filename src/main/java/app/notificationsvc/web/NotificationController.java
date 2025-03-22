package app.notificationsvc.web;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.service.NotificationService;
import app.notificationsvc.web.dto.NotificationPreferenceResponse;
import app.notificationsvc.web.dto.OrderConfirmationEmailRequest;
import app.notificationsvc.web.dto.UpsertNotificationPreference;
import app.notificationsvc.web.dto.WelcomeEmailRequest;
import app.notificationsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> upsertNotificationPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference) {

        NotificationPreference notificationPreference = notificationService.upsertPreference(upsertNotificationPreference);

        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/emails/welcome")
    public ResponseEntity<Void> sendWelcomeEmail(@RequestBody WelcomeEmailRequest welcomeEmailRequest) {
        notificationService.sendWelcomeEmail(welcomeEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/emails/order/confirmation")
    public ResponseEntity<Void> sendOrderConfirmationEmail(@RequestBody OrderConfirmationEmailRequest orderConfirmationEmailRequest) {
        notificationService.sendOrderConfirmationEmail(orderConfirmationEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
