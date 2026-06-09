package com.evently.evt_open_service.publisher;

import com.evently.evt_open_service.dto.kafka.KafkaEventWrapper;
import com.evently.evt_open_service.dto.response.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventKafkaPublisherImpl implements EventPublisher {

    public static final String TOPIC_EVENT_PUBLISHED = "event.published";
    public static final String TOPIC_EVENT_STATUS_CHANGED = "event.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(String entityId, String eventType, Object payload) {
        String messageId = UUID.randomUUID().toString();
        
        KafkaEventWrapper wrapper = KafkaEventWrapper.builder()
                .eventId(messageId)
                .eventType(eventType)
                .occurredAt(LocalDateTime.now().toString())
                .payload(payload)
                .build();

        String topic = determineTopic(eventType, payload);

        log.info("Publishing Kafka event: messageId={}, entityId={}, eventType={}, topic={}", 
                messageId, entityId, eventType, topic);


        kafkaTemplate.send(topic, entityId, wrapper);
    }

    private String determineTopic(String eventType, Object payload) {
        if ("EVENT_CREATED".equals(eventType)) {
            return TOPIC_EVENT_PUBLISHED;
        }
        
        if (payload instanceof EventResponse eventResponse) {
            if (eventResponse.getStatus() != null) {
                switch (eventResponse.getStatus()) {
                    case PUBLISHED:
                        return TOPIC_EVENT_PUBLISHED;
                    case CANCELLED:
                    case SOLD_OUT:
                    case DRAFT:
                        return TOPIC_EVENT_STATUS_CHANGED;
                }
            }
        }
        
        return TOPIC_EVENT_STATUS_CHANGED;
    }
}
