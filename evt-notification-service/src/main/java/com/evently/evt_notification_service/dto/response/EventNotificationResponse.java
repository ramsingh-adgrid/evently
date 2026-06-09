package com.evently.evt_notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNotificationResponse {
    private String id;
    private String eventId;
    private String entityId;
    private String eventName;
    private String eventType;
    private String city;
    private String category;
    private String status;
    private LocalDateTime receivedAt;
    private boolean processed;
}
