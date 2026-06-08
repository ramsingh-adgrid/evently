package com.evently.evt_bff.dto.request;

import com.evently.evt_bff.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private Status status;
}
