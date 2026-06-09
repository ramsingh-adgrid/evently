package com.evently.evt_open_service.controller;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.evently.evt_open_service.dto.response.ListEventsResponse;
import com.evently.evt_open_service.service.EventGrpcClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventOpenController.class)
class EventOpenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventGrpcClientService eventGrpcClientService;

    @Test
    void createEvent_shouldReturnCreatedEvent() throws Exception {
        // Arrange
        CreateEventRequest request = CreateEventRequest.builder()
                .eventName("Rock Show")
                .organizerName("Concerts LLC")
                .organizerMobile("9876543210")
                .city("Dallas")
                .category(Category.MUSIC)
                .build();

        UUID eventId = UUID.randomUUID();
        EventResponse response = EventResponse.builder()
                .id(eventId)
                .eventName("Rock Show")
                .organizerName("Concerts LLC")
                .organizerMobile("9876543210")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .createdOn(LocalDateTime.now())
                .modifiedOn(LocalDateTime.now())
                .build();

        when(eventGrpcClientService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/open/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.eventName").value("Rock Show"));
    }

    @Test
    void getEvent_shouldReturnEvent() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponse response = EventResponse.builder()
                .id(eventId)
                .eventName("Rock Show")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .build();

        when(eventGrpcClientService.getEvent(eventId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/open/v1/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.eventName").value("Rock Show"));
    }

    @Test
    void listEvents_shouldReturnList() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponse response = EventResponse.builder()
                .id(eventId)
                .eventName("Rock Show")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.DRAFT)
                .build();

        ListEventsResponse listResponse = ListEventsResponse.builder()
                .events(List.of(response))
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .build();

        when(eventGrpcClientService.listEvents("Dallas", Category.MUSIC, Status.DRAFT, 0, 10))
                .thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/open/v1/events")
                        .param("city", "Dallas")
                        .param("category", "MUSIC")
                        .param("status", "DRAFT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.events[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void updateStatus_shouldReturnUpdatedEvent() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(Status.PUBLISHED)
                .build();

        EventResponse response = EventResponse.builder()
                .id(eventId)
                .eventName("Rock Show")
                .city("Dallas")
                .category(Category.MUSIC)
                .status(Status.PUBLISHED)
                .build();

        when(eventGrpcClientService.updateStatus(eq(eventId), any(UpdateStatusRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/open/v1/events/" + eventId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void getStats_shouldReturnStats() throws Exception {
        // Arrange
        StatsResponse response = StatsResponse.builder()
                .totalEvents(10)
                .byStatus(Map.of("DRAFT", 5L, "PUBLISHED", 5L))
                .byCategory(Map.of("MUSIC", 10L))
                .build();

        when(eventGrpcClientService.getStats()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/open/v1/events/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEvents").value(10))
                .andExpect(jsonPath("$.data.byStatus.DRAFT").value(5));
    }
}
