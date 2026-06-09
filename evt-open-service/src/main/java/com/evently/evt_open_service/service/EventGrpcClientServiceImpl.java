package com.evently.evt_open_service.service;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.common.evt_commom_util.exception.BadRequestException;
import com.common.evt_commom_util.exception.DuplicateResourceException;
import com.common.evt_commom_util.exception.ResourceNotFoundException;
import com.common.evt_commom_util.mapper.EventEnumMapper;
import com.common.evt_commom_util.constants.CommonConstants;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.mapper.EventGrpcClientMapper;
import com.evently.evt_open_service.publisher.EventPublisher;
import com.evently.grpc.EventServiceGrpc;
import com.evently.grpc.GetStatsRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

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
        com.evently.grpc.CreateEventRequest protoRequest = EventGrpcClientMapper.toProto(request);

        try {
            com.evently.grpc.EventResponse protoResponse = eventServiceStub.createEvent(protoRequest);
            EventResponse response = EventGrpcClientMapper.toDto(protoResponse);
            eventPublisher.publishEvent(response.getId().toString(), CommonConstants.EVENT_TYPE_CREATED, response);
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
            return EventGrpcClientMapper.toDto(protoResponse);
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
                    .events(protoResponse.getEventsList().stream().map(EventGrpcClientMapper::toDto).collect(Collectors.toList()))
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
        com.evently.grpc.UpdateEventStatusRequest protoRequest = EventGrpcClientMapper.toProto(id, request);

        try {
            com.evently.grpc.EventResponse protoResponse = eventServiceStub.updateEventStatus(protoRequest);
            EventResponse response = EventGrpcClientMapper.toDto(protoResponse);
            eventPublisher.publishEvent(response.getId().toString(), CommonConstants.EVENT_TYPE_STATUS_CHANGED, response);
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
            return EventGrpcClientMapper.toDto(protoResponse);
        } catch (StatusRuntimeException e) {
            throw translateException(e);
        }
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
