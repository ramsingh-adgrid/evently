package com.evently.evt_bff.controller;

import com.evently.evt_bff.client.EventOpenServiceClient;
import com.evently.evt_bff.dto.request.CreateEventRequest;
import com.evently.evt_bff.dto.request.UpdateStatusRequest;
import com.evently.evt_bff.dto.response.ApiResponse;
import com.evently.evt_bff.dto.response.EventResponse;
import com.evently.evt_bff.dto.response.ListEventsResponse;
import com.evently.evt_bff.dto.response.StatsResponse;
import com.evently.evt_bff.enums.Category;
import com.evently.evt_bff.enums.Status;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventBffController {

    private final EventOpenServiceClient eventOpenServiceClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventOpenServiceClient.createEvent(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable UUID id) {
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
    public ApiResponse<EventResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return eventOpenServiceClient.updateStatus(id, request);
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return eventOpenServiceClient.getStats();
    }
}
