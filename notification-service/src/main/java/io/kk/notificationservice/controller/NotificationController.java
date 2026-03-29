package io.kk.notificationservice.controller;

import io.kk.notificationservice.dto.NotificationDTO;
import io.kk.notificationservice.dto.NotificationRequestDTO;
import io.kk.notificationservice.integration.WebSocketService;
import io.kk.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final WebSocketService webSocketService;

    @GetMapping(value = "/unread/{id}")
    ResponseEntity<List<NotificationDTO>> getUnreadUserNotifications(@PathVariable Integer id) {
        var result = notificationService.getUnreadUserNotifications(id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping(value = "/{id}")
    ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @RabbitListener(queues = "#{getQueue}")
    public void createEvent(@Valid NotificationRequestDTO notificationRequestDTO) {
        var notification = notificationService.createNotification(notificationRequestDTO);
        webSocketService.sendNotification(notification, notificationRequestDTO.recipientId());
    }
}
