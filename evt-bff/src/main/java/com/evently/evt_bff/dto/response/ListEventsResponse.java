package com.evently.evt_bff.dto.response;

import lombok.Builder;
import lombok.Getter;
import com.common.evt_commom_util.dto.response.EventResponse;

import java.util.List;

@Getter
@Builder
public class ListEventsResponse {
    private List<EventResponse> events;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
