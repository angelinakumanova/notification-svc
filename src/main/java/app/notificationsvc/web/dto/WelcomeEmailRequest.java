package app.notificationsvc.web.dto;

import app.notificationsvc.model.EmailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WelcomeEmailRequest {

    @NotBlank
    private String subject;

    @NotNull
    private EmailType emailType;

    @NotNull
    private UUID userId;

    @NotBlank
    private String userFirstName;
}
