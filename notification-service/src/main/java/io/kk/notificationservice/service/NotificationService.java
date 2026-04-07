package io.kk.notificationservice.service;

import io.kk.notificationservice.dto.NotificationDTO;
import io.kk.notificationservice.exception.NotificationNotFoundException;
import io.kk.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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