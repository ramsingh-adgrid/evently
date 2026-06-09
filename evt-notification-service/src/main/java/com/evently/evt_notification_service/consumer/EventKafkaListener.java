package com.evently.evt_notification_service.consumer;

import com.evently.evt_notification_service.dto.KafkaEventWrapper;
import com.evently.evt_notification_service.service.EventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventKafkaListener {

    private final EventNotificationService eventNotificationService;

    @KafkaListener(
            topics = {"event.published", "event.status.changed"},
            groupId = "${spring.kafka.consumer.group-id:evently-notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(@Payload KafkaEventWrapper wrapper, Acknowledgment ack) {
        log.info("Received event wrapper: eventId={}, eventType={}", wrapper.getEventId(), wrapper.getEventType());

        try {
            if (wrapper.getPayload() != null && "POISON_PILL".equalsIgnoreCase(wrapper.getPayload().getEventName())) {
                log.error("Poison pill message detected. Simulating processing failure to trigger DLT.");
                throw new RuntimeException("Simulated processing failure (Poison Pill)");
            }

            eventNotificationService.processEventNotification(wrapper);
            ack.acknowledge();
            log.debug("Acknowledged message: {}", wrapper.getEventId());
        } catch (Exception e) {
            log.error("Error in Kafka listener while processing eventId={}. Rethrowing for error handler/DLT.",
                    wrapper.getEventId(), e);
            throw e;
        }
    }
}
