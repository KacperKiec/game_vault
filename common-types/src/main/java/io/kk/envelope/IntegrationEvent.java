package io.kk.envelope;

import io.kk.type.EventType;
import lombok.Data;

import java.time.Instant;

@Data
public class IntegrationEvent<T> {
    private EventType eventType;
    private String sourceService;
    private Long userId;
    private Instant occurredAt;
    private T payload;

    public IntegrationEvent(EventType eventType, String sourceService, Long userId, Instant occurredAt, T payload) {}
}
