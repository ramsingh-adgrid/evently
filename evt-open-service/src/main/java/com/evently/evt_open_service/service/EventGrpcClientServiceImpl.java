package com.evently.evt_open_service.service;

import com.evently.evt_open_service.dto.request.CreateEventRequest;
import com.evently.evt_open_service.dto.request.UpdateStatusRequest;
import com.evently.evt_open_service.dto.response.EventResponse;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.dto.response.StatsResponse;
import com.evently.evt_open_service.enums.Category;
import com.evently.evt_open_service.enums.Status;
import com.evently.evt_open_service.exception.BadRequestException;
import com.evently.evt_open_service.exception.DuplicateResourceException;
import com.evently.evt_open_service.exception.ResourceNotFoundException;
import com.evently.evt_open_service.mapper.EventEnumMapper;
import com.evently.evt_open_service.publisher.EventPublisher;
import com.evently.grpc.EventServiceGrpc;
import com.evently.grpc.GetStatsRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventGrpcClientServiceImpl implements EventGrpcClientService {

    private final EventPublisher eventPublisher;

    @GrpcClient("evt-core-service")
    private EventServiceGrpc.EventServiceBlockingStub eventServiceStub;

    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        com.evently.grpc.CreateEventRequest protoRequest = com.evently.grpc.CreateEventRequest.newBuilder()
                .setEventName(request.getEventName())
                .setOrganizerName(request.getOrganizerName())
                .setOrganizerMobile(request.getOrganizerMobile())
                .setCity(request.getCity())
                .setCategory(EventEnumMapper.toProto(request.getCategory()))
                .build();

        try {
            com.evently.grpc.EventResponse protoResponse = eventServiceStub.createEvent(protoRequest);
            EventResponse response = mapToDto(protoResponse);
            eventPublisher.publishEvent(response.getId().toString(), "EVENT_CREATED", response);
            return response;
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
    }

    @Override
    public EventResponse getEvent(UUID id) {
        com.evently.grpc.GetEventRequest protoRequest = com.evently.grpc.GetEventRequest.newBuilder()
                .setId(id.toString())
                .build();

        try {
            com.evently.grpc.EventResponse protoResponse = eventServiceStub.getEvent(protoRequest);
            return mapToDto(protoResponse);
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
    }

    @Override
    public ListEventsResponse listEvents(String city, Category category, Status status, int page, int size) {
        com.evently.grpc.ListEventsRequest.Builder builder = com.evently.grpc.ListEventsRequest.newBuilder()
                .setPage(page)
                .setSize(size);

        if (city != null) {
            builder.setCity(city);
        }
        if (category != null) {
            builder.setCategory(EventEnumMapper.toProto(category));
        }
        if (status != null) {
            builder.setStatus(EventEnumMapper.toProto(status));
        }

        try {
            com.evently.grpc.ListEventsResponse protoResponse = eventServiceStub.listEvents(builder.build());
            return ListEventsResponse.builder()
                    .events(protoResponse.getEventsList().stream().map(this::mapToDto).collect(Collectors.toList()))
                    .totalElements(protoResponse.getTotalElements())
                    .totalPages(protoResponse.getTotalPages())
                    .currentPage(protoResponse.getCurrentPage())
                    .build();
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
    }

    @Override
    public EventResponse updateStatus(UUID id, UpdateStatusRequest request) {
        com.evently.grpc.UpdateEventStatusRequest protoRequest = com.evently.grpc.UpdateEventStatusRequest.newBuilder()
                .setId(id.toString())
                .setStatus(EventEnumMapper.toProto(request.getStatus()))
                .build();

        try {
            com.evently.grpc.EventResponse protoResponse = eventServiceStub.updateEventStatus(protoRequest);
            EventResponse response = mapToDto(protoResponse);
            eventPublisher.publishEvent(response.getId().toString(), "EVENT_STATUS_CHANGED", response);
            return response;
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
    }

    @Override
    public StatsResponse getStats() {
        GetStatsRequest protoRequest = GetStatsRequest.newBuilder().build();

        try {
            com.evently.grpc.StatsResponse protoResponse = eventServiceStub.getEventStats(protoRequest);
            return StatsResponse.builder()
                    .totalEvents(protoResponse.getTotalEvents())
                    .byStatus(protoResponse.getByStatusMap())
                    .byCategory(protoResponse.getByCategoryMap())
                    .build();
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
    }

    private EventResponse mapToDto(com.evently.grpc.EventResponse proto) {
        return EventResponse.builder()
                .id(UUID.fromString(proto.getId()))
                .eventName(proto.getEventName())
                .organizerName(proto.getOrganizerName())
                .organizerMobile(proto.getOrganizerMobile())
                .city(proto.getCity())
                .category(EventEnumMapper.toCategory(proto.getCategory()))
                .status(EventEnumMapper.toStatus(proto.getStatus()))
                .createdOn(LocalDateTime.parse(proto.getCreatedOn()))
                .modifiedOn(LocalDateTime.parse(proto.getModifiedOn()))
                .build();
    }

    private RuntimeException translateException(StatusRuntimeException e) {
        log.error("gRPC execution failed with status: {}, description: {}", e.getStatus().getCode(), e.getStatus().getDescription());
        switch (e.getStatus().getCode()) {
            case NOT_FOUND:
                return new ResourceNotFoundException(e.getStatus().getDescription());
            case ALREADY_EXISTS:
                return new DuplicateResourceException(e.getStatus().getDescription());
            case INVALID_ARGUMENT:
                return new BadRequestException(e.getStatus().getDescription());
            default:
                return new RuntimeException(e.getStatus().getDescription() != null ? e.getStatus().getDescription() : e.getMessage());
        }
    }
}
