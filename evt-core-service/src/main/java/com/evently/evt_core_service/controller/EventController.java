package com.evently.evt_core_service.controller;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.ApiResponse;
import com.common.evt_commom_util.dto.EventDTO;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.evently.evt_core_service.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventDTO> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        return ApiResponse.success(eventService.createEvent(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getEvent(@PathVariable UUID id) {
        return ApiResponse.success(eventService.getEvent(id));
    }

    @GetMapping
    public ApiResponse<Page<EventDTO>> listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(eventService.listEvents(city, category, status, pageable));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<EventDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ApiResponse.success(eventService.updateStatus(id, request));
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.success(eventService.getStats());
    }
}