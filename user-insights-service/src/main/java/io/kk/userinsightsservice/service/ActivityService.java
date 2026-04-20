package io.kk.userinsightsservice.service;

import io.kk.userinsightsservice.dto.ActivityDTO;
import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void addActivity(ActivityDTO activityDTO) {
        Activity activity = new Activity();
        activity.setUserId(activityDTO.userId());
        activity.setActivityType(activityDTO.activityType());
        activity.setOccurredAt(activityDTO.occurredAt());
        activity.setRelatedGameId(activityDTO.relatedGameId());
        activity.setMetadata(activityDTO.metadata());
        activityRepository.save(activity);
    }
}
