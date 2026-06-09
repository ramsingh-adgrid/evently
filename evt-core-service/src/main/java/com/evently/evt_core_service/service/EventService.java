package com.evently.evt_core_service.service;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.EventDTO;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventDTO createEvent(CreateEventRequest request);
    EventDTO getEvent(UUID id);
    Page<EventDTO> listEvents(String city, Category category, Status status, Pageable pageable);
    EventDTO updateStatus(UUID id, UpdateStatusRequest request);
    StatsResponse getStats();
}