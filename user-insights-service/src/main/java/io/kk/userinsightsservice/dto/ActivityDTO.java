package io.kk.userinsightsservice.dto;

import io.kk.userinsightsservice.model.postgres.ActivityType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ActivityDTO(
        @NotNull
        Long userId,

        @NotNull
        ActivityType activityType,

        @NotNull
        LocalDateTime occurredAt,

        @NotNull
        Long relatedGameId,

        @NotNull
        Map<String, Object> metadata
) {
}
