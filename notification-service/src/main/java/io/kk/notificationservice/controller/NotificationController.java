package io.kk.notificationservice.controller;

import io.kk.notificationservice.dto.NotificationDTO;
import io.kk.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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
}
