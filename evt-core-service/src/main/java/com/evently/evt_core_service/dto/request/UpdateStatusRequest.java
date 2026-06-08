package com.evently.evt_core_service.dto.request;

import com.evently.evt_core_service.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
   private Status status;
}
