package com.evently.evt_notification_service.controller;

import com.common.evt_commom_util.dto.response.ApiResponse;
import com.evently.evt_notification_service.dto.response.CityDashboardResponse;
import com.evently.evt_notification_service.dto.response.EventNotificationResponse;
import com.evently.evt_notification_service.service.EventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final EventNotificationService eventNotificationService;

    @GetMapping("/notifications")
    public ApiResponse<List<EventNotificationResponse>> getNotifications(
            @RequestParam String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to fetch notifications for entityId: {}, page: {}, size: {}", entityId, page, size);
        List<EventNotificationResponse> dtos = eventNotificationService.getNotificationsByEntityId(entityId, page, size);
        return ApiResponse.success(dtos);
    }

    @GetMapping("/dashboard/{city}")
    public ApiResponse<CityDashboardResponse> getDashboard(@PathVariable String city) {
        log.info("Received request to fetch dashboard for city: {}", city);
        CityDashboardResponse response = eventNotificationService.getDashboardByCity(city);
        return ApiResponse.success(response);
    }
}
