package app.notificationsvc.web.dto;

import app.notificationsvc.model.EmailType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderShippedEmailRequest {

    @NotBlank
    private String subject;

    @NotNull
    private EmailType emailType;

    @NotNull
    private UUID userId;

    @NotBlank
    private Long orderId;

    @NotNull
    @Min(0)
    private BigDecimal totalAmount;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String courier;

    @NotBlank
    private String address;
}
