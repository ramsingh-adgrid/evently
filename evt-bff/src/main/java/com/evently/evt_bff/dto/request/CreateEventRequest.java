package com.evently.evt_bff.dto.request;

import com.evently.evt_bff.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Eventname cannot be blank")
    private String eventName;

    @NotBlank(message = "OrganizerName cannot be blank")
    private String organizerName;

    @NotBlank(message = "organizer mobile is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "mobile must be of 10 numbers")
    private String organizerMobile;

    @NotBlank(message = "city is required")
    private String city;

    @NotNull(message = "category must be specified")
    private Category category;
}
