package com.evently.evt_bff.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StatsResponse {
    private long totalEvents;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
}
