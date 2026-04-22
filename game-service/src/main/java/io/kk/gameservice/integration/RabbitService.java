package io.kk.gameservice.integration;

import io.kk.envelope.IntegrationEvent;
import io.kk.gameservice.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.notification.queue}")
    private String notificationQueueName;

    @Value("${app.rabbit.dashboard.queue}")
    private String dashboardQueueName;

    public void sendNotification(NotificationRequestDTO dto) {
        rabbitTemplate.convertAndSend(notificationQueueName, dto);
    }

    public void sendDashboardEvent(IntegrationEvent<?> event) {
        rabbitTemplate.convertAndSend(dashboardQueueName, event);
    }
}