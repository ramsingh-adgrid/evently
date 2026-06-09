package com.evently.evt_open_service.publisher;

import com.common.evt_commom_util.dto.EventDTO;

public interface EventPublisher {
    void publishEvent(String eventId, String eventType, EventDTO payload);
}
