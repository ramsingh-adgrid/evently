package com.common.evt_commom_util.dto.kafka;

import com.common.evt_commom_util.dto.response.EventResponse;
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
    private EventResponse payload;
}
