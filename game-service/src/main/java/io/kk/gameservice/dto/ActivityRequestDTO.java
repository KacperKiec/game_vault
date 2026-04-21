package io.kk.gameservice.dto;

import io.kk.gameservice.util.ActivityType;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ActivityRequestDTO(
        Long userId,
        ActivityType activityType,
        LocalDateTime occurredAt,
        Long relatedGameId,
        Map<String, Object> metadata
) implements Serializable {
}
