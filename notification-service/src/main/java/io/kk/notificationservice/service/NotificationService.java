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

/**
 * Service responsible for managing user notifications.
 * It provides functionality to create new notifications, mark existing ones as read,
 * and retrieve a list of unread notifications for a specific user.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Creates and persists a new notification based on the provided request data.
     * The notification is initialized with a "unread" status and the current timestamp.
     *
     * @param dto The data transfer object containing notification details such as title,
     * content, recipient ID, type, and optional metadata.
     * @return A {@link NotificationDTO} representing the newly created notification.
     */
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

    /**
     * Updates the status of a specific notification to "read".
     *
     * @param notificationId The unique identifier of the notification to update.
     * @throws NotificationNotFoundException if no notification is found with the given ID.
     */
    public void markNotificationAsRead(Long notificationId) {
        var notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotificationNotFoundException("Notification not found")
        );

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Retrieves all notifications for a specific user that have not yet been read.
     *
     * @param userId The unique identifier of the recipient user.
     * @return A list of {@link NotificationDTO} objects containing the unread notifications.
     */
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