package com.evently.evt_notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {
    private String id;
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private String category;
    private String status;
    private Object createdOn;
    private Object modifiedOn;
}
