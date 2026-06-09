package com.evently.evt_open_service.service;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.evently.evt_open_service.dto.response.ListEventsResponse;

import java.util.UUID;

public interface EventGrpcClientService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEvent(UUID id);
    ListEventsResponse listEvents(String city, Category category, Status status, int page, int size);
    EventResponse updateStatus(UUID id, UpdateStatusRequest request);
    StatsResponse getStats();
}
