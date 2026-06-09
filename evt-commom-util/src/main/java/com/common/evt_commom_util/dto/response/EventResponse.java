package com.common.evt_commom_util.dto.response;

import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private Category category;
    private Status status;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
}
