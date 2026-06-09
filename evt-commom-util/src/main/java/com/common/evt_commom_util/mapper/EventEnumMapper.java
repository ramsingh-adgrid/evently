package com.common.evt_commom_util.mapper;

import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.evently.grpc.CategoryProto;
import com.evently.grpc.EventStatusProto;

public class EventEnumMapper {

    public static Category toCategory(CategoryProto proto) {
        return switch (proto) {
            case MUSIC -> Category.MUSIC;
            case SPORTS -> Category.SPORTS;
            case COMEDY -> Category.COMEDY;
            case WORKSHOP -> Category.WORKSHOP;
            case OTHER -> Category.OTHER;
            default -> throw new IllegalArgumentException(
                    "Unknown category: " + proto);
        };
    }

    public static CategoryProto toProto(Category category) {
        return switch (category) {
            case MUSIC -> CategoryProto.MUSIC;
            case SPORTS -> CategoryProto.SPORTS;
            case COMEDY -> CategoryProto.COMEDY;
            case WORKSHOP -> CategoryProto.WORKSHOP;
            case OTHER -> CategoryProto.OTHER;
        };
    }

    public static Status toStatus(EventStatusProto proto) {
        return switch (proto) {
            case DRAFT -> Status.DRAFT;
            case PUBLISHED -> Status.PUBLISHED;
            case CANCELLED -> Status.CANCELLED;
            case SOLD_OUT -> Status.SOLD_OUT;
            default -> throw new IllegalArgumentException(
                    "Unknown status: " + proto);
        };
    }

    public static EventStatusProto toProto(Status status) {
        return switch (status) {
            case DRAFT -> EventStatusProto.DRAFT;
            case PUBLISHED -> EventStatusProto.PUBLISHED;
            case CANCELLED -> EventStatusProto.CANCELLED;
            case SOLD_OUT -> EventStatusProto.SOLD_OUT;
        };
    }
}
