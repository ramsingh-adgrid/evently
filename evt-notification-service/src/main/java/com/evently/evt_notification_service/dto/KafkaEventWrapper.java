package com.evently.evt_notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEventWrapper {
    private String eventId;
    private String eventType;
    private String occurredAt;
    private EventPayload payload;
}
