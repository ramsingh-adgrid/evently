package com.evently.evt_bff.dto.response;

import lombok.Builder;
import lombok.Getter;
import com.common.evt_commom_util.dto.EventDTO;

import java.util.List;

@Getter
@Builder
public class ListEventsResponse {
    private List<EventDTO> events;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
