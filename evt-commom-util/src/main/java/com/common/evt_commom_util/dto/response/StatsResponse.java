package com.common.evt_commom_util.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private long totalEvents;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
}
