package com.evently.evt_bff.dto.response;

import com.evently.evt_bff.enums.Category;
import com.evently.evt_bff.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
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
