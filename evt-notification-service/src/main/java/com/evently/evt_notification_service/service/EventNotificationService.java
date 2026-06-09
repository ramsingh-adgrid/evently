package com.evently.evt_notification_service.service;

import com.common.evt_commom_util.dto.kafka.KafkaEventWrapper;
import com.evently.evt_notification_service.dto.response.CityDashboardResponse;
import com.evently.evt_notification_service.dto.response.EventNotificationResponse;

import java.util.List;

public interface EventNotificationService {
    void processEventNotification(KafkaEventWrapper wrapper);
    List<EventNotificationResponse> getNotificationsByEntityId(String entityId, int page, int size);
    CityDashboardResponse getDashboardByCity(String city);
}
