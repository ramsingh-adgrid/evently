package com.evently.evt_core_service.grpc;

import com.evently.evt_core_service.dto.request.CreateEventRequest;
import com.evently.evt_core_service.dto.request.UpdateStatusRequest;
import com.evently.evt_core_service.enums.Category;
import com.evently.evt_core_service.enums.Status;
import com.evently.evt_core_service.exception.BadRequestException;
import com.evently.evt_core_service.exception.DuplicateResourceException;
import com.evently.evt_core_service.exception.ResourceNotFoundException;
import com.evently.evt_core_service.mapper.EventEnumMapper;
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
            CreateEventRequest request = new CreateEventRequest();
            request.setEventName(protoRequest.getEventName());
            request.setOrganizerName(protoRequest.getOrganizerName());
            request.setOrganizerMobile(protoRequest.getOrganizerMobile());
            request.setCity(protoRequest.getCity());
            request.setCategory(EventEnumMapper.toCategory(protoRequest.getCategory()));

            com.evently.evt_core_service.dto.response.EventResponse response =
                    eventService.createEvent(request);

            responseObserver.onNext(toProto(response));
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
            com.evently.evt_core_service.dto.response.EventResponse response =
                    eventService.getEvent(UUID.fromString(protoRequest.getId()));

            responseObserver.onNext(toProto(response));
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
            String city = protoRequest.getCity().isEmpty()
                    ? null : protoRequest.getCity();
            Category category = protoRequest.getCategory() == CategoryProto.CATEGORY_UNSPECIFIED
                    ? null : EventEnumMapper.toCategory(protoRequest.getCategory());
            Status status = protoRequest.getStatus() == EventStatusProto.STATUS_UNSPECIFIED
                    ? null : EventEnumMapper.toStatus(protoRequest.getStatus());

            Page<com.evently.evt_core_service.dto.response.EventResponse> page =
                    eventService.listEvents(
                            city, category, status,
                            PageRequest.of(protoRequest.getPage(), protoRequest.getSize()));

            ListEventsResponse.Builder builder = ListEventsResponse.newBuilder()
                    .setTotalElements(page.getTotalElements())
                    .setTotalPages(page.getTotalPages())
                    .setCurrentPage(page.getNumber());

            page.getContent().forEach(event -> builder.addEvents(toProto(event)));

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
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(EventEnumMapper.toStatus(protoRequest.getStatus()));

            com.evently.evt_core_service.dto.response.EventResponse response =
                    eventService.updateStatus(
                            UUID.fromString(protoRequest.getId()), request);

            responseObserver.onNext(toProto(response));
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
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> statsMap = (java.util.Map<String, Object>) eventService.getStats();
            long totalEvents = (long) statsMap.get("totalEvents");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Long> byStatus = (java.util.Map<String, Long>) statsMap.get("byStatus");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Long> byCategory = (java.util.Map<String, Long>) statsMap.get("byCategory");

            com.evently.grpc.StatsResponse protoStats =
                    com.evently.grpc.StatsResponse.newBuilder()
                            .setTotalEvents(totalEvents)
                            .putAllByStatus(byStatus)
                            .putAllByCategory(byCategory)
                            .build();

            responseObserver.onNext(protoStats);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    private com.evently.grpc.EventResponse toProto(
            com.evently.evt_core_service.dto.response.EventResponse response) {
        return com.evently.grpc.EventResponse.newBuilder()
                .setId(response.getId().toString())
                .setEventName(response.getEventName())
                .setOrganizerName(response.getOrganizerName())
                .setOrganizerMobile(response.getOrganizerMobile())
                .setCity(response.getCity())
                .setCategory(EventEnumMapper.toProto(response.getCategory()))
                .setStatus(EventEnumMapper.toProto(response.getStatus()))
                .setCreatedOn(response.getCreatedOn().toString())
                .setModifiedOn(response.getModifiedOn().toString())
                .build();
    }
}