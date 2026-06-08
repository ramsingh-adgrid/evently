package com.evently.evt_open_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ListEventsResponse {
    private List<EventResponse> events;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
