package app.notificationsvc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WelcomeEmailRequest {
    @NotNull
    private UUID userId;

    @NotNull
    @NotBlank
    private String userFirstName;
}
