package io.kk.userinsightsservice.controller;

import io.kk.userinsightsservice.dto.ActivityDTO;
import io.kk.userinsightsservice.service.ActivityService;
import io.kk.userinsightsservice.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final DashboardService dashboardService;

    @RabbitListener(queues = "#{getActivityQueue}")
    public void createEvent(@Valid ActivityDTO activityDTO) {
        activityService.addActivity(activityDTO);
        dashboardService.updateRecentActivity(activityDTO);
    }
}
