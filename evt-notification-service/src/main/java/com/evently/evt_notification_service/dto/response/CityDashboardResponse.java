package com.evently.evt_notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDashboardResponse {
    private String city;
    private long totalEvents;
    private long publishedEvents;
    private Map<String, Long> eventsByCategory;
    private LocalDateTime lastUpdatedAt;
}
