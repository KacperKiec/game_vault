package io.kk.userinsightsservice.dto;

import io.kk.userinsightsservice.model.postgres.ActivityType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record ActivityDTO(
        @NotNull
        Long userId,

        @NotNull
        ActivityType activityType,

        @NotNull
        Instant occurredAt,

        @NotNull
        Long relatedGameId,

        @NotNull
        Map<String, Object> metadata
) {
}
