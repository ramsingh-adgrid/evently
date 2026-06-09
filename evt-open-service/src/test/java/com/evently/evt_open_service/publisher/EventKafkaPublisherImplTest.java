package com.evently.evt_open_service.publisher;

import com.common.evt_commom_util.constants.CommonConstants;
import com.common.evt_commom_util.dto.kafka.KafkaEventWrapper;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventKafkaPublisherImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventKafkaPublisherImpl eventPublisher;

    @Test
    void publishEvent_shouldSendToPublishedTopicWhenEventCreated() {
        // Arrange
        String entityId = UUID.randomUUID().toString();
        EventResponse payload = EventResponse.builder()
                .id(UUID.fromString(entityId))
                .eventName("Concert")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .build();

        // Act
        eventPublisher.publishEvent(entityId, CommonConstants.EVENT_TYPE_CREATED, payload);

        // Assert
        ArgumentCaptor<KafkaEventWrapper> wrapperCaptor = ArgumentCaptor.forClass(KafkaEventWrapper.class);
        verify(kafkaTemplate).send(eq(CommonConstants.TOPIC_EVENT_PUBLISHED), eq(entityId), wrapperCaptor.capture());

        KafkaEventWrapper capturedWrapper = wrapperCaptor.getValue();
        assertThat(capturedWrapper).isNotNull();
        assertThat(capturedWrapper.getEventType()).isEqualTo(CommonConstants.EVENT_TYPE_CREATED);
        assertThat(capturedWrapper.getPayload()).isEqualTo(payload);
    }

    @Test
    void publishEvent_shouldSendToStatusChangedTopicWhenEventStatusChanged() {
        // Arrange
        String entityId = UUID.randomUUID().toString();
        EventResponse payload = EventResponse.builder()
                .id(UUID.fromString(entityId))
                .eventName("Concert")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.PUBLISHED)
                .build();

        // Act
        eventPublisher.publishEvent(entityId, CommonConstants.EVENT_TYPE_STATUS_CHANGED, payload);

        // Assert
        ArgumentCaptor<KafkaEventWrapper> wrapperCaptor = ArgumentCaptor.forClass(KafkaEventWrapper.class);
        verify(kafkaTemplate).send(eq(CommonConstants.TOPIC_EVENT_STATUS_CHANGED), eq(entityId), wrapperCaptor.capture());

        KafkaEventWrapper capturedWrapper = wrapperCaptor.getValue();
        assertThat(capturedWrapper).isNotNull();
        assertThat(capturedWrapper.getEventType()).isEqualTo(CommonConstants.EVENT_TYPE_STATUS_CHANGED);
        assertThat(capturedWrapper.getPayload()).isEqualTo(payload);
    }
}
