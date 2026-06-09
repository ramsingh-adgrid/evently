package com.common.evt_commom_util.constants;

public final class CommonConstants {

    private CommonConstants() {
        // Prevent instantiation
    }


    public static final String TOPIC_EVENT_PUBLISHED = "event.published";
    public static final String TOPIC_EVENT_STATUS_CHANGED = "event.status.changed";
    public static final String TOPIC_EVENT_PUBLISHED_DLT = "event.published.dlt";
    public static final String TOPIC_EVENT_STATUS_CHANGED_DLT = "event.status.changed.dlt";


    public static final String EVENT_TYPE_CREATED = "EVENT_CREATED";
    public static final String EVENT_TYPE_STATUS_CHANGED = "EVENT_STATUS_CHANGED";
    public static final String EVENT_NAME_POISON_PILL = "POISON_PILL";
}
