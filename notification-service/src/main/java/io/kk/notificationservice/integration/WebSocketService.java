package io.kk.notificationservice.integration;

import io.kk.notificationservice.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationDTO notification, Long recipientId) {
        messagingTemplate.convertAndSend("/topic/notification/" + recipientId, notification);
    }
}
