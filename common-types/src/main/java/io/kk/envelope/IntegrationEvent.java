package io.kk.envelope;

import io.kk.type.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class IntegrationEvent<T> {
    private UUID eventId;
    private EventType eventType;
    private String sourceService;
    private Long userId;
    private LocalDateTime occurredAt;
    private T payload;

    public IntegrationEvent(EventType eventType, String sourceService, Long userId, LocalDateTime occurredAt, T payload) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.userId = userId;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

}
