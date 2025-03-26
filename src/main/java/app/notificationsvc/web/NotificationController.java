package app.notificationsvc.web;

import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.service.NotificationService;
import app.notificationsvc.web.dto.*;
import app.notificationsvc.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getUserNotificationPreference(@RequestParam(name = "userId") UUID userId) {

        NotificationPreference notificationPreference = notificationService.getByUserId(userId);

        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> upsertNotificationPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference) {

        NotificationPreference notificationPreference = notificationService.upsertPreference(upsertNotificationPreference);

        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> changeNotificationPreference(@RequestParam(name = "userId") UUID userId, @RequestParam(name = "enabled") boolean enabled) {

        NotificationPreference notificationPreference = notificationService.changeNotificationPreference(userId, enabled);

        NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping("/emails/welcome")
    public ResponseEntity<Void> sendWelcomeEmail(@RequestBody WelcomeEmailRequest welcomeEmailRequest) {
        notificationService.sendWelcomeEmail(welcomeEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/emails/order/confirmation")
    public ResponseEntity<Void> sendOrderConfirmationEmail(@RequestBody OrderCreateEmailRequest orderConfirmationEmailRequest) {
        notificationService.sendNewOrderEmail(orderConfirmationEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/emails/order/new")
    public ResponseEntity<Void> sendNewOrderEmail(@RequestBody OrderCreateEmailRequest orderNewEmailRequest) {
        notificationService.sendNewOrderEmail(orderNewEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("emails/order/shipped")
    public ResponseEntity<Void> sendOrderShippedEmail(@RequestBody OrderShippedEmailRequest orderShippedEmailRequest) {
        notificationService.sendShippedOrderEmail(orderShippedEmailRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/emails/newsletter")
    public ResponseEntity<Void> sendNewsletter() {
        notificationService.sendNewsletter();

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
