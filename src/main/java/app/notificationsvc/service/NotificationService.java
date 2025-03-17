package app.notificationsvc.service;

import app.notificationsvc.model.Notification;
import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationStatus;
import app.notificationsvc.repository.NotificationPreferenceRepository;
import app.notificationsvc.web.dto.UpsertNotificationPreference;
import app.notificationsvc.web.dto.WelcomeEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public NotificationService(NotificationPreferenceRepository preferenceRepository, JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.preferenceRepository = preferenceRepository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference dto) {

        Optional<NotificationPreference> optionalNotificationPreference = preferenceRepository.findByUserId(dto.getUserId());

        if (optionalNotificationPreference.isPresent()) {
            NotificationPreference notificationPreference = optionalNotificationPreference.get();
            notificationPreference.toBuilder()
                    .type(dto.getNotificationType())
                    .isNewsletterEnabled(dto.isNewsletterEnabled())
                    .contactData(dto.getContactData()).build();

            return preferenceRepository.save(notificationPreference);
        } else {
            NotificationPreference preference = NotificationPreference.builder()
                    .userId(dto.getUserId())
                    .type(dto.getNotificationType())
                    .isNewsletterEnabled(dto.isNewsletterEnabled())
                    .contactData(dto.getContactData())
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            return preferenceRepository.save(preference);
        }

    }

    public void sendWelcomeEmail(WelcomeEmailRequest welcomeEmailRequest) {
        String subject = "Welcome To Dripify!";

        Context context = new Context();
        context.setVariable("firstName", welcomeEmailRequest.getUserFirstName());

        String body = templateEngine.process("welcome-email", context);

        sendMail(welcomeEmailRequest.getUserId(), subject, body);
    }

    private void sendMail(UUID userId, String subject, String body) {
        NotificationPreference userNotificationPreference = getByUserId(userId);

        Notification notification = Notification.builder()
                .userId(userNotificationPreference.getUserId())
                .subject(subject)
                .body(body)
                .createdOn(LocalDateTime.now())
                .build();

        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
                messageHelper.setTo(userNotificationPreference.getContactData());
                messageHelper.setSubject(subject);
                messageHelper.setText(body, true);
            });

            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("Failed to send notification to user with id: %s due to %s".formatted(userId, e.getMessage()));
        }
    }

    private NotificationPreference getByUserId(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElseThrow(() -> new NullPointerException("Notification preference for user: " + userId + " not found"));
    }

}
