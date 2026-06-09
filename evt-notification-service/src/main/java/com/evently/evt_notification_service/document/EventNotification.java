package com.evently.evt_notification_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "event_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNotification {
    @Id
    private String id;

    @Indexed(unique = true)
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
