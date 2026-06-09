package com.evently.evt_bff.client;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.ApiResponse;
import com.common.evt_commom_util.dto.EventDTO;
import com.evently.evt_bff.dto.response.ListEventsResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "evt-open-service", url = "${EVT_OPEN_SERVICE_URL:http://localhost:8082}")
public interface EventFeignClient {

    @PostMapping("/open/v1/events")
    ApiResponse<EventDTO> createEvent(@RequestBody CreateEventRequest request);

    @GetMapping("/open/v1/events/{id}")
    ApiResponse<EventDTO> getEvent(@PathVariable("id") UUID id);

    @GetMapping("/open/v1/events")
    ApiResponse<ListEventsResponse> listEvents(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    @PatchMapping("/open/v1/events/{id}/status")
    ApiResponse<EventDTO> updateStatus(
            @PathVariable("id") UUID id,
            @RequestBody UpdateStatusRequest request);

    @GetMapping("/open/v1/events/stats")
    ApiResponse<StatsResponse> getStats();
}
