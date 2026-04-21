package io.kk.userinsightsservice.model.mongo;

import io.kk.userinsightsservice.model.postgres.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class DashboardActivityItem {
    private ActivityType type;
    private LocalDateTime occurredAt;
    private Long gameId;
    private String gameTitle;
    private Map<String, Object> details;
}
