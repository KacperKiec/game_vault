package io.kk.userinsightsservice.controller;

import io.kk.envelope.IntegrationEvent;
import io.kk.userinsightsservice.service.ActivityService;
import io.kk.userinsightsservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsightsEventListener {

    private final DashboardService dashboardService;
    private final ActivityService activityService;

    @RabbitListener(queues = "#{getDashboardQueue}")
    void handleInsightsEvent(IntegrationEvent<?> event) {
        if (!activityService.isProcessed(event.getEventId())) {
            activityService.addActivity(event);
        }
        if (!dashboardService.isEventApplied(event.getUserId(), event.getEventId())) {
            dashboardService.handleDashboardEvent(event);
        }
    }

}