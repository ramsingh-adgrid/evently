package com.evently.evt_core_service.service;

import com.evently.evt_core_service.dto.request.CreateEventRequest;
import com.evently.evt_core_service.dto.request.UpdateStatusRequest;
import com.evently.evt_core_service.dto.response.EventResponse;
import com.evently.evt_core_service.enums.Category;
import com.evently.evt_core_service.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEvent(UUID id);
    Page<EventResponse> listEvents(String city, Category category, Status status, Pageable pageable);
    EventResponse updateStatus(UUID id, UpdateStatusRequest request);
    Object getStats();
}