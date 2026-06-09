package com.evently.evt_open_service.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaEventWrapper {
    private String eventId;
    private String eventType;
    private String occurredAt;
    private Object payload;
}
