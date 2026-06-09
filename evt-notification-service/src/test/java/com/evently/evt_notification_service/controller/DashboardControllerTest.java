package com.evently.evt_notification_service.controller;

import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
import com.evently.evt_notification_service.service.EventNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventNotificationService eventNotificationService;

    @Test
    void getNotifications_shouldReturnNotificationsList() throws Exception {
        EventNotification notification = EventNotification.builder()
                .id("notif-1")
                .eventId("msg-1")
                .entityId("event-1")
                .eventName("Salsa Night")
                .eventType("EVENT_CREATED")
                .city("Dallas")
                .processed(true)
                .build();

        when(eventNotificationService.getNotificationsByEntityId("event-1")).thenReturn(List.of(notification));

        mockMvc.perform(get("/v1/notifications").param("entityId", "event-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("msg-1"))
                .andExpect(jsonPath("$[0].entityId").value("event-1"))
                .andExpect(jsonPath("$[0].eventName").value("Salsa Night"));
    }

    @Test
    void getDashboard_shouldReturnCityDashboard() throws Exception {
        CityDashboard dashboard = CityDashboard.builder()
                .city("Dallas")
                .totalEvents(5)
                .publishedEvents(2)
                .eventsByCategory(new HashMap<>() {{ put("MUSIC", 3L); }})
                .build();

        when(eventNotificationService.getDashboardByCity("Dallas")).thenReturn(dashboard);

        mockMvc.perform(get("/v1/dashboard/Dallas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Dallas"))
                .andExpect(jsonPath("$.totalEvents").value(5))
                .andExpect(jsonPath("$.publishedEvents").value(2))
                .andExpect(jsonPath("$.eventsByCategory.MUSIC").value(3));
    }
}
