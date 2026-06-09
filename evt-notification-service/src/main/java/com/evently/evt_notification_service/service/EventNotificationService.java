package com.evently.evt_notification_service.service;

import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
import com.common.evt_commom_util.dto.kafka.KafkaEventWrapper;

import java.util.List;

public interface EventNotificationService {
    void processEventNotification(KafkaEventWrapper wrapper);
    List<EventNotification> getNotificationsByEntityId(String entityId, int page, int size);
    CityDashboard getDashboardByCity(String city);
}
