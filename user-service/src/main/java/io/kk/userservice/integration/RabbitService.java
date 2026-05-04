package io.kk.userservice.integration;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.UserRegisteredPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.dashboard.queue}")
    private String queueName;

    public void sendDashboardEvent(IntegrationEvent<UserRegisteredPayload> event) {
        rabbitTemplate.convertAndSend(queueName, event);
    }
}
