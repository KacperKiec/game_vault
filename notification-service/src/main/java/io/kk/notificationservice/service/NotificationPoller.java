package io.kk.notificationservice.service;

import io.kk.notificationservice.dto.NotificationDTO;
import io.kk.notificationservice.integration.WebSocketService;
import io.kk.notificationservice.model.Notification;
import io.kk.notificationservice.repository.impl.JdbcNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPoller {

    private final JdbcNotificationRepository notificationRepository;
    private final WebSocketService webSocketService;

    @Scheduled(fixedDelay = 10000)
    public void pollForNewNotifications() {
        List<Notification> newNotifications = notificationRepository.findUnsentToWebSocket();

        for (Notification notif : newNotifications) {
            NotificationDTO dto = NotificationDTO.builder()
                    .id(notif.getId())
                    .type(notif.getType())
                    .title(notif.getTitle())
                    .content(notif.getContent())
                    .metadata(notif.getMetadata())
                    .createdAt(notif.getCreatedAt())
                    .read(notif.getRead())
                    .build();
            webSocketService.sendNotification(dto, notif.getRecipientId());
            notificationRepository.markAsSentToWebSocket(notif.getId());
        }
    }
}
