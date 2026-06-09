package com.evently.evt_core_service.grpc;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.EventDTO;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.common.evt_commom_util.exception.BadRequestException;
import com.common.evt_commom_util.exception.DuplicateResourceException;
import com.common.evt_commom_util.exception.ResourceNotFoundException;
import com.evently.evt_core_service.service.EventService;
import com.evently.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventGrpcServiceTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventGrpcService eventGrpcService;

    @Mock
    private StreamObserver<com.evently.grpc.EventResponse> eventResponseObserver;

    @Mock
    private StreamObserver<com.evently.grpc.ListEventsResponse> listEventsResponseObserver;

    @Mock
    private StreamObserver<com.evently.grpc.StatsResponse> statsResponseObserver;

    @Test
    void createEvent_success() {
        // Arrange
        com.evently.grpc.CreateEventRequest protoRequest = com.evently.grpc.CreateEventRequest.newBuilder()
                .setEventName("Festival")
                .setOrganizerName("Org")
                .setOrganizerMobile("1111111111")
                .setCity("Austin")
                .setCategory(CategoryProto.MUSIC)
                .build();

        UUID eventId = UUID.randomUUID();
        EventDTO serviceResponse = EventDTO.builder()
                .id(eventId)
                .eventName("Festival")
                .organizerName("Org")
                .organizerMobile("1111111111")
                .city("Austin")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .createdOn(LocalDateTime.now())
                .modifiedOn(LocalDateTime.now())
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(serviceResponse);

        // Act
        eventGrpcService.createEvent(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<com.evently.grpc.EventResponse> responseCaptor = ArgumentCaptor.forClass(com.evently.grpc.EventResponse.class);
        verify(eventResponseObserver).onNext(responseCaptor.capture());
        verify(eventResponseObserver).onCompleted();

        com.evently.grpc.EventResponse capturedResponse = responseCaptor.getValue();
        assertThat(capturedResponse.getId()).isEqualTo(eventId.toString());
        assertThat(capturedResponse.getEventName()).isEqualTo("Festival");
    }

    @Test
    void createEvent_alreadyExists() {
        // Arrange
        com.evently.grpc.CreateEventRequest protoRequest = com.evently.grpc.CreateEventRequest.newBuilder()
                .setEventName("Festival")
                .setOrganizerName("Org")
                .setOrganizerMobile("1111111111")
                .setCity("Austin")
                .setCategory(CategoryProto.MUSIC)
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class)))
                .thenThrow(new DuplicateResourceException("Mobile already exists"));

        // Act
        eventGrpcService.createEvent(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(eventResponseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertThat(error).isInstanceOf(io.grpc.StatusRuntimeException.class);
        assertThat(error.getMessage()).contains("ALREADY_EXISTS");
    }

    @Test
    void getEvent_success() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        GetEventRequest protoRequest = GetEventRequest.newBuilder().setId(eventId.toString()).build();

        EventDTO serviceResponse = EventDTO.builder()
                .id(eventId)
                .eventName("Festival")
                .organizerName("Org")
                .organizerMobile("1111111111")
                .city("Austin")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .build();

        when(eventService.getEvent(eventId)).thenReturn(serviceResponse);

        // Act
        eventGrpcService.getEvent(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<com.evently.grpc.EventResponse> responseCaptor = ArgumentCaptor.forClass(com.evently.grpc.EventResponse.class);
        verify(eventResponseObserver).onNext(responseCaptor.capture());
        verify(eventResponseObserver).onCompleted();
    }

    @Test
    void getEvent_notFound() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        GetEventRequest protoRequest = GetEventRequest.newBuilder().setId(eventId.toString()).build();
        when(eventService.getEvent(eventId)).thenThrow(new ResourceNotFoundException("Not found"));

        // Act
        eventGrpcService.getEvent(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(eventResponseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertThat(error).isInstanceOf(io.grpc.StatusRuntimeException.class);
        assertThat(error.getMessage()).contains("NOT_FOUND");
    }

    @Test
    void listEvents_success() {
        // Arrange
        ListEventsRequest protoRequest = ListEventsRequest.newBuilder()
                .setCity("Austin")
                .setCategory(CategoryProto.MUSIC)
                .setStatus(EventStatusProto.DRAFT)
                .setPage(0)
                .setSize(10)
                .build();

        UUID eventId = UUID.randomUUID();
        EventDTO eventResponse = EventDTO.builder()
                .id(eventId)
                .eventName("Festival")
                .organizerName("Org")
                .organizerMobile("1111111111")
                .city("Austin")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .build();

        Page<EventDTO> page = new PageImpl<>(List.of(eventResponse));
        when(eventService.listEvents(eq("Austin"), eq(Category.MUSIC), eq(Status.DRAFT), any(Pageable.class)))
                .thenReturn(page);

        // Act
        eventGrpcService.listEvents(protoRequest, listEventsResponseObserver);

        // Assert
        ArgumentCaptor<com.evently.grpc.ListEventsResponse> responseCaptor = ArgumentCaptor.forClass(com.evently.grpc.ListEventsResponse.class);
        verify(listEventsResponseObserver).onNext(responseCaptor.capture());
        verify(listEventsResponseObserver).onCompleted();

        com.evently.grpc.ListEventsResponse listResponse = responseCaptor.getValue();
        assertThat(listResponse.getTotalElements()).isEqualTo(1);
        assertThat(listResponse.getEvents(0).getId()).isEqualTo(eventId.toString());
    }

    @Test
    void updateEventStatus_success() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UpdateEventStatusRequest protoRequest = UpdateEventStatusRequest.newBuilder()
                .setId(eventId.toString())
                .setStatus(EventStatusProto.PUBLISHED)
                .build();

        EventDTO serviceResponse = EventDTO.builder()
                .id(eventId)
                .eventName("Festival")
                .organizerName("Org")
                .organizerMobile("1111111111")
                .city("Austin")
                .category(Category.MUSIC)
                .status(Status.PUBLISHED)
                .build();

        when(eventService.updateStatus(eq(eventId), any(UpdateStatusRequest.class)))
                .thenReturn(serviceResponse);

        // Act
        eventGrpcService.updateEventStatus(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<com.evently.grpc.EventResponse> responseCaptor = ArgumentCaptor.forClass(com.evently.grpc.EventResponse.class);
        verify(eventResponseObserver).onNext(responseCaptor.capture());
        verify(eventResponseObserver).onCompleted();
    }

    @Test
    void updateEventStatus_badRequest() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UpdateEventStatusRequest protoRequest = UpdateEventStatusRequest.newBuilder()
                .setId(eventId.toString())
                .setStatus(EventStatusProto.PUBLISHED)
                .build();

        when(eventService.updateStatus(eq(eventId), any(UpdateStatusRequest.class)))
                .thenThrow(new BadRequestException("Invalid transition"));

        // Act
        eventGrpcService.updateEventStatus(protoRequest, eventResponseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(eventResponseObserver).onError(errorCaptor.capture());
        Throwable error = errorCaptor.getValue();
        assertThat(error).isInstanceOf(io.grpc.StatusRuntimeException.class);
        assertThat(error.getMessage()).contains("INVALID_ARGUMENT");
    }

    @Test
    void getEventStats_success() {
        // Arrange
        GetStatsRequest protoRequest = GetStatsRequest.newBuilder().build();
        StatsResponse serviceResponse = StatsResponse.builder()
                .totalEvents(100)
                .byStatus(Map.of("DRAFT", 100L))
                .byCategory(Map.of("MUSIC", 100L))
                .build();

        when(eventService.getStats()).thenReturn(serviceResponse);

        // Act
        eventGrpcService.getEventStats(protoRequest, statsResponseObserver);

        // Assert
        ArgumentCaptor<com.evently.grpc.StatsResponse> responseCaptor = ArgumentCaptor.forClass(com.evently.grpc.StatsResponse.class);
        verify(statsResponseObserver).onNext(responseCaptor.capture());
        verify(statsResponseObserver).onCompleted();

        com.evently.grpc.StatsResponse statsResponse = responseCaptor.getValue();
        assertThat(statsResponse.getTotalEvents()).isEqualTo(100L);
    }
}
