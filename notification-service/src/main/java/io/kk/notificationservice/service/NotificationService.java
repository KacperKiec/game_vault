package io.kk.notificationservice.service;

import io.kk.notificationservice.dto.NotificationDTO;
import io.kk.notificationservice.dto.NotificationRequestDTO;
import io.kk.notificationservice.exception.NotificationNotFoundException;
import io.kk.notificationservice.model.Notification;
import io.kk.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDTO createNotification(NotificationRequestDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.title());
        notification.setContent(dto.content());
        notification.setType(dto.type());
        notification.setRecipientId(dto.recipientId());
        notification.setMetadata(dto.metadata());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        var saved = notificationRepository.save(notification);

        return NotificationDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .type(saved.getType())
                .metadata(saved.getMetadata())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void markNotificationAsRead(Long notificationId) {
        var notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotificationNotFoundException("Notification not found")
        );

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUnreadUserNotifications(Integer userId) {
        return notificationRepository.findByRecipientIdAndRead(userId, false)
                .stream().map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .metadata(n.getMetadata())
                        .createdAt(n.getCreatedAt())
                        .read(n.getRead())
                        .build()).toList();
    }
}
