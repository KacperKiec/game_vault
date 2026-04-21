package io.kk.userinsightsservice.controller;

import io.kk.envelope.IntegrationEvent;
import io.kk.userinsightsservice.exception.DashboardException;
import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping()
    ResponseEntity<DashboardDocument> getUserDashboard(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getUserDashboard(userId));
    }

    @RabbitListener(queues = "#{getDashboardQueue}")
    void updateDashboard(IntegrationEvent<?> event) {
        try {
            dashboardService.handleDashboardEvent(event);
        } catch (DashboardException e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
