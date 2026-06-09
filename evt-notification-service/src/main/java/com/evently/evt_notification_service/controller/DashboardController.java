package com.evently.evt_notification_service.controller;

import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
import com.evently.evt_notification_service.service.EventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final EventNotificationService eventNotificationService;

    @GetMapping("/notifications")
    public ResponseEntity<List<EventNotification>> getNotifications(@RequestParam String entityId) {
        log.info("Received request to fetch notifications for entityId: {}", entityId);
        List<EventNotification> notifications = eventNotificationService.getNotificationsByEntityId(entityId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/dashboard/{city}")
    public ResponseEntity<CityDashboard> getDashboard(@PathVariable String city) {
        log.info("Received request to fetch dashboard for city: {}", city);
        CityDashboard dashboard = eventNotificationService.getDashboardByCity(city);
        return ResponseEntity.ok(dashboard);
    }
}
