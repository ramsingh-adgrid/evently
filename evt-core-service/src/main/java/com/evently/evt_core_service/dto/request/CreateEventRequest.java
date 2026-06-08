package com.evently.evt_core_service.dto.request;

import com.evently.evt_core_service.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor

public class CreateEventRequest {

    @NotBlank(message = "Eventname cannot be blank")
    String eventName;

    @NotBlank(message = "OrganizerName cannot be blank")
    String organizerName;

    @NotBlank(message = "organizer mobile is required")
     @Pattern(regexp = "^[0-9]{10}$", message = "mobile must be of 10 numbers")
    String organizerMobile;

    @NotBlank(message = "city is required")
    String city;

    @NotNull(message = "category must be specified ")
    Category category;
}
