package com.evently.evt_open_service.mapper;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.mapper.EventEnumMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventGrpcClientMapper {

    public static com.evently.grpc.CreateEventRequest toProto(CreateEventRequest dto) {
        return com.evently.grpc.CreateEventRequest.newBuilder()
                .setEventName(dto.getEventName())
                .setOrganizerName(dto.getOrganizerName())
                .setOrganizerMobile(dto.getOrganizerMobile())
                .setCity(dto.getCity())
                .setCategory(EventEnumMapper.toProto(dto.getCategory()))
                .build();
    }

    public static com.evently.grpc.UpdateEventStatusRequest toProto(UUID id, UpdateStatusRequest dto) {
        return com.evently.grpc.UpdateEventStatusRequest.newBuilder()
                .setId(id.toString())
                .setStatus(EventEnumMapper.toProto(dto.getStatus()))
                .build();
    }

    public static EventResponse toDto(com.evently.grpc.EventResponse proto) {
        return EventResponse.builder()
                .id(UUID.fromString(proto.getId()))
                .eventName(proto.getEventName())
                .organizerName(proto.getOrganizerName())
                .organizerMobile(proto.getOrganizerMobile())
                .city(proto.getCity())
                .category(EventEnumMapper.toCategory(proto.getCategory()))
                .status(EventEnumMapper.toStatus(proto.getStatus()))
                .createdOn(toLocalDateTime(proto.getCreatedOn()))
                .modifiedOn(toLocalDateTime(proto.getModifiedOn()))
                .build();
    }

    private static java.time.LocalDateTime toLocalDateTime(com.google.protobuf.Timestamp timestamp) {
        if (timestamp == null || (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0)) {
            return null;
        }
        return java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                java.time.ZoneId.systemDefault()
        );
    }

    public static StatsResponse toDto(com.evently.grpc.StatsResponse proto) {
        return StatsResponse.builder()
                .totalEvents(proto.getTotalEvents())
                .byStatus(proto.getByStatusMap())
                .byCategory(proto.getByCategoryMap())
                .build();
    }
}
