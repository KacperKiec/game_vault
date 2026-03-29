package io.kk.notificationservice.repository.impl;

import io.kk.notificationservice.model.Notification;
import io.kk.notificationservice.repository.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, Long>, NotificationRepository {
}
