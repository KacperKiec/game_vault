package io.kk.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;
    private Long recipientId;
    private NotificationType type;
    private String title;
    private String content;
    private Map<String, Object> metadata;
    private Boolean read = false;
    private LocalDateTime createdAt;
    private Boolean wsSent = false;
}
