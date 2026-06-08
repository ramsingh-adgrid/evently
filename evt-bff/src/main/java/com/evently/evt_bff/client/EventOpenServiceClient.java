package com.evently.evt_bff.client;

import com.evently.evt_bff.dto.request.CreateEventRequest;
import com.evently.evt_bff.dto.request.UpdateStatusRequest;
import com.evently.evt_bff.dto.response.ApiResponse;
import com.evently.evt_bff.dto.response.EventResponse;
import com.evently.evt_bff.dto.response.ListEventsResponse;
import com.evently.evt_bff.dto.response.StatsResponse;
import com.evently.evt_bff.enums.Category;
import com.evently.evt_bff.enums.Status;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "evt-open-service", url = "${EVT_OPEN_SERVICE_URL:http://localhost:8082}")
public interface EventOpenServiceClient {

    @PostMapping("/open/v1/events")
    ApiResponse<EventResponse> createEvent(@RequestBody CreateEventRequest request);

    @GetMapping("/open/v1/events/{id}")
    ApiResponse<EventResponse> getEvent(@PathVariable("id") UUID id);

    @GetMapping("/open/v1/events")
    ApiResponse<ListEventsResponse> listEvents(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    @PatchMapping("/open/v1/events/{id}/status")
    ApiResponse<EventResponse> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusRequest request);

    @GetMapping("/open/v1/events/stats")
    ApiResponse<StatsResponse> getStats();
}
