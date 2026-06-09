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
import com.common.evt_commom_util.mapper.EventEnumMapper;
import com.evently.evt_core_service.mapper.EventGrpcMapper;
import com.evently.evt_core_service.service.EventService;
import com.evently.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class EventGrpcService extends EventServiceGrpc.EventServiceImplBase {

    private final EventService eventService;

    @Override
    public void createEvent(com.evently.grpc.CreateEventRequest protoRequest,
                            StreamObserver<com.evently.grpc.EventResponse> responseObserver) {
        try {
            CreateEventRequest request = EventGrpcMapper.toDto(protoRequest);
            EventDTO response = eventService.createEvent(request);
            responseObserver.onNext(EventGrpcMapper.toProto(response));
            responseObserver.onCompleted();
        } catch (DuplicateResourceException e) {
            responseObserver.onError(
                    io.grpc.Status.ALREADY_EXISTS
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getEvent(GetEventRequest protoRequest,
                         StreamObserver<com.evently.grpc.EventResponse> responseObserver) {
        try {
            EventDTO response = eventService.getEvent(UUID.fromString(protoRequest.getId()));
            responseObserver.onNext(EventGrpcMapper.toProto(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void listEvents(ListEventsRequest protoRequest,
                           StreamObserver<ListEventsResponse> responseObserver) {
        try {
            String city = protoRequest.getCity().isEmpty() ? null : protoRequest.getCity();
            Category category = protoRequest.getCategory() == CategoryProto.CATEGORY_UNSPECIFIED
                    ? null : EventEnumMapper.toCategory(protoRequest.getCategory());
            Status status = protoRequest.getStatus() == EventStatusProto.STATUS_UNSPECIFIED
                    ? null : EventEnumMapper.toStatus(protoRequest.getStatus());

            Page<EventDTO> page = eventService.listEvents(
                    city, category, status,
                    PageRequest.of(protoRequest.getPage(), protoRequest.getSize()));

            ListEventsResponse.Builder builder = ListEventsResponse.newBuilder()
                    .setTotalElements(page.getTotalElements())
                    .setTotalPages(page.getTotalPages())
                    .setCurrentPage(page.getNumber());

            page.getContent().forEach(event -> builder.addEvents(EventGrpcMapper.toProto(event)));

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void updateEventStatus(UpdateEventStatusRequest protoRequest,
                                  StreamObserver<com.evently.grpc.EventResponse> responseObserver) {
        try {
            UpdateStatusRequest request = EventGrpcMapper.toDto(protoRequest);
            EventDTO response = eventService.updateStatus(UUID.fromString(protoRequest.getId()), request);
            responseObserver.onNext(EventGrpcMapper.toProto(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (BadRequestException e) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getEventStats(GetStatsRequest protoRequest,
                              StreamObserver<com.evently.grpc.StatsResponse> responseObserver) {
        try {
            StatsResponse stats = eventService.getStats();
            responseObserver.onNext(EventGrpcMapper.toProto(stats));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}