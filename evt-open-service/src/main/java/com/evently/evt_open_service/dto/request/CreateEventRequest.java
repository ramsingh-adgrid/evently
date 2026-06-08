package com.evently.evt_open_service.dto.request;

import com.evently.evt_open_service.enums.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateEventRequest {
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private Category category;
}
