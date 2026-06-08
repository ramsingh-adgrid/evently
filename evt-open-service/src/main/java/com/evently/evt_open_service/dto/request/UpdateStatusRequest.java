package com.evently.evt_open_service.dto.request;

import com.evently.evt_open_service.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateStatusRequest {
    private Status status;
}
