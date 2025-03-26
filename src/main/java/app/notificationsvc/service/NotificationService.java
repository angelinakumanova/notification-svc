package app.notificationsvc.service;

import app.notificationsvc.model.EmailType;
import app.notificationsvc.model.Notification;
import app.notificationsvc.model.NotificationPreference;
import app.notificationsvc.model.NotificationStatus;
import app.notificationsvc.repository.NotificationPreferenceRepository;
import app.notificationsvc.repository.NotificationRepository;
import app.notificationsvc.web.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public NotificationService(NotificationPreferenceRepository preferenceRepository, NotificationRepository notificationRepository, JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.preferenceRepository = preferenceRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public NotificationPreference getByUserId(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElseThrow(() -> new NullPointerException("Notification preference for user: " + userId + " not found"));
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
        Context context = new Context();
        context.setVariable("firstName", welcomeEmailRequest.getUserFirstName());

        String body = templateEngine.process(welcomeEmailRequest.getEmailType().getTemplate(), context);

        sendMail(welcomeEmailRequest.getUserId(), welcomeEmailRequest.getEmailType(), welcomeEmailRequest.getSubject(), body);
    }

    public void sendNewOrderEmail(OrderCreateEmailRequest orderInfoEmailRequest) {
        Context context = new Context();
        context.setVariable("fullName", orderInfoEmailRequest.getFullName());
        context.setVariable("address", orderInfoEmailRequest.getAddress());
        context.setVariable("phoneNumber", orderInfoEmailRequest.getPhoneNumber());
        context.setVariable("courier", orderInfoEmailRequest.getCourier());
        context.setVariable("paymentMethod", orderInfoEmailRequest.getPaymentMethod());

        String body = templateEngine.process(orderInfoEmailRequest.getEmailType().getTemplate(), context);

        sendMail(orderInfoEmailRequest.getUserId(), orderInfoEmailRequest.getEmailType(), orderInfoEmailRequest.getSubject(), body);
    }

    public void sendShippedOrderEmail(OrderShippedEmailRequest orderShippedEmailRequest) {
        Context context = new Context();
        context.setVariable("orderId", orderShippedEmailRequest.getOrderId());
        context.setVariable("totalAmount", orderShippedEmailRequest.getTotalAmount());
        context.setVariable("address", orderShippedEmailRequest.getAddress());
        context.setVariable("courier", orderShippedEmailRequest.getCourier());
        context.setVariable("paymentMethod", orderShippedEmailRequest.getPaymentMethod());

        String body = templateEngine.process(orderShippedEmailRequest.getEmailType().getTemplate(), context);

        sendMail(orderShippedEmailRequest.getUserId(), orderShippedEmailRequest.getEmailType(), orderShippedEmailRequest.getSubject(), body);
    }

    public void sendNewsletter() {
        Context context = new Context();

        String subject = "Your weekly update is here!!!\uD83D\uDC8C";
        String body = templateEngine.process(EmailType.NEWSLETTER.getTemplate(), context);

        List<NotificationPreference> allByIsNewsletterEnabledTrue  = preferenceRepository.findAllByIsNewsletterEnabledTrue();

        allByIsNewsletterEnabledTrue.forEach(userPreference -> sendMail(userPreference.getUserId(), EmailType.NEWSLETTER, subject, body));
    }

    public NotificationPreference changeNotificationPreference(UUID userId, boolean enabled) {
        NotificationPreference notificationPreference = getByUserId(userId);
        notificationPreference.setNewsletterEnabled(enabled);
        return preferenceRepository.save(notificationPreference);
    }

    private void sendMail(UUID userId, EmailType templateName, String subject, String body) {
        NotificationPreference userNotificationPreference = getByUserId(userId);

        Notification notification = Notification.builder()
                .userId(userNotificationPreference.getUserId())
                .subject(subject)
                .emailType(templateName)
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

        notificationRepository.save(notification);
    }



}
