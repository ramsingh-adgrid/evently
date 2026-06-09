package com.evently.evt_open_service.dto.response;

import com.common.evt_commom_util.dto.EventDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ListEventsResponse {
    private List<EventDTO> events;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
