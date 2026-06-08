package com.evently.evt_open_service.service;

import com.evently.evt_open_service.dto.request.CreateEventRequest;
import com.evently.evt_open_service.dto.request.UpdateStatusRequest;
import com.evently.evt_open_service.dto.response.EventResponse;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.dto.response.StatsResponse;
import com.evently.evt_open_service.enums.Category;
import com.evently.evt_open_service.enums.Status;

import java.util.UUID;

public interface EventGrpcClientService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEvent(UUID id);
    ListEventsResponse listEvents(String city, Category category, Status status, int page, int size);
    EventResponse updateStatus(UUID id, UpdateStatusRequest request);
    StatsResponse getStats();
}
