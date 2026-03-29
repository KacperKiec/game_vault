package io.kk.notificationservice.dto;

import io.kk.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationRequestDTO(
    @NotNull
    Long recipientId,

    @NotNull
    NotificationType type,

    @Size(max = 50)
    @NotNull
    @NotBlank
    String title,

    @Size(max = 255)
    @NotNull
    @NotBlank
    String content,

    @NotNull
    Map<String, Object> metadata
) {
}
