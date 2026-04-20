package io.kk.userinsightsservice.controller;

import io.kk.userinsightsservice.dto.ActivityDTO;
import io.kk.userinsightsservice.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @RabbitListener(queues = "#{getActivityQueue}")
    public void createEvent(@Valid ActivityDTO activityDTO) {
        activityService.addActivity(activityDTO);
    }
}
