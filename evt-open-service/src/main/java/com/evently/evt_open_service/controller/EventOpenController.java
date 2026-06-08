package com.evently.evt_open_service.controller;

import com.evently.evt_open_service.dto.request.CreateEventRequest;
import com.evently.evt_open_service.dto.request.UpdateStatusRequest;
import com.evently.evt_open_service.dto.response.ApiResponse;
import com.evently.evt_open_service.dto.response.EventResponse;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.dto.response.StatsResponse;
import com.evently.evt_open_service.enums.Category;
import com.evently.evt_open_service.enums.Status;
import com.evently.evt_open_service.service.EventGrpcClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/open/v1/events")
@RequiredArgsConstructor
public class EventOpenController {

    private final EventGrpcClientService eventGrpcClientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        return ApiResponse.success(eventGrpcClientService.createEvent(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable UUID id) {
        return ApiResponse.success(eventGrpcClientService.getEvent(id));
    }

    @GetMapping
    public ApiResponse<ListEventsResponse> listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(eventGrpcClientService.listEvents(city, category, status, page, size));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<EventResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest request) {
        return ApiResponse.success(eventGrpcClientService.updateStatus(id, request));
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.success(eventGrpcClientService.getStats());
    }
}
