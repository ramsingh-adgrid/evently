package com.evently.evt_core_service.service;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEvent(UUID id);
    Page<EventResponse> listEvents(String city, Category category, Status status, Pageable pageable);
    EventResponse updateStatus(UUID id, UpdateStatusRequest request);
    StatsResponse getStats();
}