package io.kk.userinsightsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kk.envelope.IntegrationEvent;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.payload.ReviewPayload;
import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ObjectMapper objectMapper;

    public void addActivity(IntegrationEvent<?> event) {

        Activity activity = new Activity();
        activity.setEventId(event.getEventId());
        activity.setUserId(event.getUserId());
        activity.setEventType(event.getEventType());
        activity.setOccurredAt(event.getOccurredAt());

        switch (event.getEventType()) {
            case USER_REGISTERED -> {
                var payload = extractPayload(event, io.kk.payload.UserRegisteredPayload.class);
                activity.setRelatedGameId(-1L);
                activity.setRelatedGameName("");
                activity.setMetadata(Map.of("username", payload.getUsername(), "email", payload.getEmail()));
            }
            case GAME_ADDED_TO_LIST, GAME_REMOVED_FROM_LIST -> {
                var payload = extractPayload(event, GameToListPayload.class);
                activity.setRelatedGameId(payload.getGameId());
                activity.setRelatedGameName(payload.getGameTitle());
                activity.setMetadata(Map.of("listType", payload.getListType()));
            }
            case GAME_MOVED_BETWEEN_LISTS -> {
                var payload = extractPayload(event, GameMovedBetweenListsPayload.class);
                activity.setRelatedGameId(payload.getGameId());
                activity.setRelatedGameName(payload.getGameTitle());
                activity.setMetadata(Map.of("fromList", payload.getFromList(), "toList", payload.getToList()));
            }
            case REVIEW_ADDED, REVIEW_DELETED -> {
                var payload = extractPayload(event, ReviewPayload.class);
                activity.setRelatedGameId(payload.getGameId());
                activity.setRelatedGameName(payload.getGameTitle());
                activity.setMetadata(Map.of("reviewId", payload.getReviewId(), "rating", payload.getRating()));
            }
        }

        activityRepository.save(activity);
    }

    public Boolean isProcessed(UUID eventId) {
        return activityRepository.existsByEventId(eventId);
    }

    private <T> T extractPayload(IntegrationEvent<?> event, Class<T> type) {
        Object payload = event.getPayload();
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }
        return objectMapper.convertValue(payload, type);
    }
}