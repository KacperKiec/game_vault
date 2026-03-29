package io.kk.notificationservice.dto;

import io.kk.notificationservice.model.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record NotificationDTO (
        Long id,
        NotificationType type,
        String title,
        String content,
        Map<String, Object>metadata,
        LocalDateTime createdAt,
        Boolean read
) {
}
