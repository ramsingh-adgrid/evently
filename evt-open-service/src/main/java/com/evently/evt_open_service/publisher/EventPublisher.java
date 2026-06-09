package com.evently.evt_open_service.publisher;

public interface EventPublisher {
    void publishEvent(String eventId, String eventType, Object payload);
}
