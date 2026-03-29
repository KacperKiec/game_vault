package io.kk.gameservice.dto;

import io.kk.gameservice.util.NotificationType;
import lombok.Builder;

import java.io.Serializable;
import java.util.Map;

@Builder
public record NotificationRequestDTO(
    Long recipientId,
    NotificationType type,
    String title,
    String content,
    Map<String, Object> metadata
) implements Serializable {
}
