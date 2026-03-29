package io.kk.notificationservice.repository;

import io.kk.notificationservice.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByRecipientIdAndRead(Integer recipientId, Boolean read);
}
