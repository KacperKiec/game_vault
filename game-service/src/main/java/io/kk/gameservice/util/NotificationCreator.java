package io.kk.gameservice.util;

import io.kk.gameservice.dto.NotificationRequestDTO;

import java.util.Map;

public class NotificationCreator {
    public static NotificationRequestDTO createNotification(NotificationType type, Long recipientId, String title, String content, Map<String, Object> metadata) {
        return NotificationRequestDTO.builder()
            .type(type)
            .recipientId(recipientId)
            .title(title)
            .content(content)
            .metadata(metadata)
            .build();
    }
}
