package com.evently.evt_core_service.mapper;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.mapper.EventEnumMapper;

public class EventGrpcMapper {

    public static CreateEventRequest toDto(com.evently.grpc.CreateEventRequest proto) {
        return CreateEventRequest.builder()
                .eventName(proto.getEventName())
                .organizerName(proto.getOrganizerName())
                .organizerMobile(proto.getOrganizerMobile())
                .city(proto.getCity())
                .category(EventEnumMapper.toCategory(proto.getCategory()))
                .build();
    }

    public static UpdateStatusRequest toDto(com.evently.grpc.UpdateEventStatusRequest proto) {
        return UpdateStatusRequest.builder()
                .status(EventEnumMapper.toStatus(proto.getStatus()))
                .build();
    }

    public static com.evently.grpc.EventResponse toProto(EventResponse dto) {
        return com.evently.grpc.EventResponse.newBuilder()
                .setId(dto.getId().toString())
                .setEventName(dto.getEventName())
                .setOrganizerName(dto.getOrganizerName())
                .setOrganizerMobile(dto.getOrganizerMobile())
                .setCity(dto.getCity())
                .setCategory(EventEnumMapper.toProto(dto.getCategory()))
                .setStatus(EventEnumMapper.toProto(dto.getStatus()))
                .setCreatedOn(toProtoTimestamp(dto.getCreatedOn()))
                .setModifiedOn(toProtoTimestamp(dto.getModifiedOn()))
                .build();
    }

    public static com.evently.grpc.StatsResponse toProto(StatsResponse dto) {
        return com.evently.grpc.StatsResponse.newBuilder()
                .setTotalEvents(dto.getTotalEvents())
                .putAllByStatus(dto.getByStatus())
                .putAllByCategory(dto.getByCategory())
                .build();
    }

    private static com.google.protobuf.Timestamp toProtoTimestamp(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return com.google.protobuf.Timestamp.getDefaultInstance();
        }
        java.time.Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
