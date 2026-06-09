package com.common.evt_commom_util.dto.request;

import com.common.evt_commom_util.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
