package io.kk.userinsightsservice.model.mongo;

import io.kk.type.EventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class DashboardActivityItem {
    private EventType type;
    private LocalDateTime occurredAt;
    private Long gameId;
    private String gameTitle;
    private Map<String, Object> details;
}
