package io.kk.envelope;

import io.kk.type.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationEvent<T> {
    private EventType eventType;
    private String sourceService;
    private Long userId;
    private LocalDateTime occurredAt;
    private T payload;
}
