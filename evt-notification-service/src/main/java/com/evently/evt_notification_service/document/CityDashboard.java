package com.evently.evt_notification_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "city_dashboards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDashboard {
    @Id
    private String city;

    private long totalEvents;
    private long publishedEvents;

    @Builder.Default
    private Map<String, Long> eventsByCategory = new HashMap<>();

    private LocalDateTime lastUpdatedAt;
}
