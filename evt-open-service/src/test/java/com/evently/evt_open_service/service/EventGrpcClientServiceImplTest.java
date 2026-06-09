package com.evently.evt_open_service.service;

import com.common.evt_commom_util.constants.CommonConstants;
import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.common.evt_commom_util.exception.BadRequestException;
import com.common.evt_commom_util.exception.DuplicateResourceException;
import com.common.evt_commom_util.exception.ResourceNotFoundException;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.publisher.EventPublisher;
import com.evently.grpc.CategoryProto;
import com.evently.grpc.EventServiceGrpc;
import com.evently.grpc.EventStatusProto;
import com.evently.grpc.GetStatsRequest;
import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventGrpcClientServiceImplTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventServiceGrpc.EventServiceBlockingStub eventServiceStub;

    @InjectMocks
    private EventGrpcClientServiceImpl eventGrpcClientService;

    private CreateEventRequest createRequest;
    private com.evently.grpc.EventResponse protoResponse;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        createRequest = CreateEventRequest.builder()
                .eventName("Indie Concert")
                .organizerName("Indie Records")
                .organizerMobile("0987654321")
                .city("Dallas")
                .category(Category.MUSIC)
                .build();

        protoResponse = com.evently.grpc.EventResponse.newBuilder()
                .setId(eventId.toString())
                .setEventName("Indie Concert")
                .setOrganizerName("Indie Records")
                .setOrganizerMobile("0987654321")
                .setCity("Dallas")
                .setCategory(CategoryProto.MUSIC)
                .setStatus(EventStatusProto.DRAFT)
                .setCreatedOn(Timestamp.newBuilder().setSeconds(1000).setNanos(0).build())
                .setModifiedOn(Timestamp.newBuilder().setSeconds(1000).setNanos(0).build())
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(eventGrpcClientService, "eventServiceStub", eventServiceStub);
    }

    @Test
    void createEvent_shouldCallStubAndPublishEvent() {
        // Arrange
        when(eventServiceStub.createEvent(any(com.evently.grpc.CreateEventRequest.class)))
                .thenReturn(protoResponse);

        // Act
        EventResponse response = eventGrpcClientService.createEvent(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getEventName()).isEqualTo("Indie Concert");
        verify(eventPublisher).publishEvent(eq(eventId.toString()), eq(CommonConstants.EVENT_TYPE_CREATED), any(EventResponse.class));
    }

    @Test
    void createEvent_shouldTranslateAlreadyExistsException() {
        // Arrange
        when(eventServiceStub.createEvent(any(com.evently.grpc.CreateEventRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.ALREADY_EXISTS.withDescription("Conflict")));

        // Act & Assert
        assertThatThrownBy(() -> eventGrpcClientService.createEvent(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Conflict");
    }

    @Test
    void getEvent_shouldReturnEventResponse() {
        // Arrange
        when(eventServiceStub.getEvent(any(com.evently.grpc.GetEventRequest.class)))
                .thenReturn(protoResponse);

        // Act
        EventResponse response = eventGrpcClientService.getEvent(eventId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
    }

    @Test
    void getEvent_shouldTranslateNotFoundException() {
        // Arrange
        when(eventServiceStub.getEvent(any(com.evently.grpc.GetEventRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription("Not Found")));

        // Act & Assert
        assertThatThrownBy(() -> eventGrpcClientService.getEvent(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Not Found");
    }

    @Test
    void listEvents_shouldReturnListEventsResponse() {
        // Arrange
        com.evently.grpc.ListEventsResponse protoListResponse = com.evently.grpc.ListEventsResponse.newBuilder()
                .addEvents(protoResponse)
                .setTotalElements(1)
                .setTotalPages(1)
                .setCurrentPage(0)
                .build();

        when(eventServiceStub.listEvents(any(com.evently.grpc.ListEventsRequest.class)))
                .thenReturn(protoListResponse);

        // Act
        ListEventsResponse response = eventGrpcClientService.listEvents("Dallas", Category.MUSIC, Status.DRAFT, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateStatus_shouldCallStubAndPublishEvent() {
        // Arrange
        UpdateStatusRequest updateRequest = UpdateStatusRequest.builder().status(Status.PUBLISHED).build();
        com.evently.grpc.EventResponse updatedProtoResponse = protoResponse.toBuilder()
                .setStatus(EventStatusProto.PUBLISHED)
                .build();

        when(eventServiceStub.updateEventStatus(any(com.evently.grpc.UpdateEventStatusRequest.class)))
                .thenReturn(updatedProtoResponse);

        // Act
        EventResponse response = eventGrpcClientService.updateStatus(eventId, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Status.PUBLISHED);
        verify(eventPublisher).publishEvent(eq(eventId.toString()), eq(CommonConstants.EVENT_TYPE_STATUS_CHANGED), any(EventResponse.class));
    }

    @Test
    void updateStatus_shouldTranslateInvalidArgumentException() {
        // Arrange
        UpdateStatusRequest updateRequest = UpdateStatusRequest.builder().status(Status.PUBLISHED).build();
        when(eventServiceStub.updateEventStatus(any(com.evently.grpc.UpdateEventStatusRequest.class)))
                .thenThrow(new StatusRuntimeException(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid transition")));

        // Act & Assert
        assertThatThrownBy(() -> eventGrpcClientService.updateStatus(eventId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid transition");
    }

    @Test
    void getStats_shouldReturnStatsResponse() {
        // Arrange
        com.evently.grpc.StatsResponse protoStatsResponse = com.evently.grpc.StatsResponse.newBuilder()
                .setTotalEvents(10)
                .putByStatus("DRAFT", 10)
                .putByCategory("MUSIC", 10)
                .build();

        when(eventServiceStub.getEventStats(any(GetStatsRequest.class)))
                .thenReturn(protoStatsResponse);

        // Act
        StatsResponse response = eventGrpcClientService.getStats();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalEvents()).isEqualTo(10);
        assertThat(response.getByStatus().get("DRAFT")).isEqualTo(10L);
    }
}
