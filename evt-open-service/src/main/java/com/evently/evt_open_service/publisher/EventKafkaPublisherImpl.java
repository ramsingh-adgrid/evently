package com.evently.evt_open_service.publisher;

import com.common.evt_commom_util.dto.kafka.KafkaEventWrapper;
import com.common.evt_commom_util.dto.EventDTO;
import com.common.evt_commom_util.constants.CommonConstants;
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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(String entityId, String eventType, EventDTO payload) {
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

        // Key the message by the entity ID to preserve per-entity ordering
        kafkaTemplate.send(topic, entityId, wrapper);
    }

    private String determineTopic(String eventType, EventDTO payload) {
        if (CommonConstants.EVENT_TYPE_CREATED.equals(eventType)) {
            return CommonConstants.TOPIC_EVENT_PUBLISHED;
        }
        return CommonConstants.TOPIC_EVENT_STATUS_CHANGED;
    }
}
