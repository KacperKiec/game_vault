package io.kk.userinsightsservice.model.mongo;

import io.kk.userinsightsservice.model.postgres.ActivityType;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class DashboardActivityItem {
    private ActivityType type;
    private Instant occurredAt;
    private Long gameId;
    private String gameTitle;
    private String coverUrl;
    private Map<String, Object> details;
}
