package app.notificationsvc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WelcomeEmailRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String bodyTemplate;

    @NotNull
    private UUID userId;

    @NotBlank
    private String userFirstName;
}
