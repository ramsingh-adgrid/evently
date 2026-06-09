package com.evently.evt_bff.controller;

import com.evently.evt_bff.client.EventFeignClient;
import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.ApiResponse;
import com.common.evt_commom_util.dto.EventDTO;
import com.evently.evt_bff.dto.response.ListEventsResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventBffController {

    private final EventFeignClient eventOpenServiceClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventDTO> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventOpenServiceClient.createEvent(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getEvent(@PathVariable UUID id) {
        return eventOpenServiceClient.getEvent(id);
    }

    @GetMapping
    public ApiResponse<ListEventsResponse> listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return eventOpenServiceClient.listEvents(city, category, status, page, size);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<EventDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return eventOpenServiceClient.updateStatus(id, request);
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return eventOpenServiceClient.getStats();
    }
}
