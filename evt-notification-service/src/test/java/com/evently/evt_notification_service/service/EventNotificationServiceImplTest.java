package com.evently.evt_notification_service.service;

import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
import com.evently.evt_notification_service.dto.EventPayload;
import com.evently.evt_notification_service.dto.KafkaEventWrapper;
import com.evently.evt_notification_service.repository.CityDashboardRepository;
import com.evently.evt_notification_service.repository.EventNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationServiceImplTest {

    @Mock
    private EventNotificationRepository eventNotificationRepository;

    @Mock
    private CityDashboardRepository cityDashboardRepository;

    @InjectMocks
    private EventNotificationServiceImpl eventNotificationService;

    private KafkaEventWrapper wrapper;
    private EventPayload payload;

    @BeforeEach
    void setUp() {
        payload = EventPayload.builder()
                .id("event-123")
                .eventName("Salsa Night")
                .city("Chicago")
                .category("MUSIC")
                .status("DRAFT")
                .build();

        wrapper = KafkaEventWrapper.builder()
                .eventId("msg-999")
                .eventType("EVENT_CREATED")
                .payload(payload)
                .build();
    }

    @Test
    void processEventNotification_shouldSkipIfDuplicateEventId() {
        // Arrange
        when(eventNotificationRepository.existsByEventId("msg-999")).thenReturn(true);

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        verify(eventNotificationRepository, never()).save(any());
        verify(cityDashboardRepository, never()).save(any());
    }

    @Test
    void processEventNotification_shouldCreateNewDashboardIfFirstEvent() {
        // Arrange
        when(eventNotificationRepository.existsByEventId("msg-999")).thenReturn(false);
        when(eventNotificationRepository.findByEntityId("event-123")).thenReturn(new ArrayList<>());
        when(cityDashboardRepository.findById("Chicago")).thenReturn(Optional.empty());

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        verify(eventNotificationRepository, times(2)).save(any(EventNotification.class));

        ArgumentCaptor<CityDashboard> dashboardCaptor = ArgumentCaptor.forClass(CityDashboard.class);
        verify(cityDashboardRepository).save(dashboardCaptor.capture());

        CityDashboard savedDashboard = dashboardCaptor.getValue();
        assertThat(savedDashboard.getCity()).isEqualTo("Chicago");
        assertThat(savedDashboard.getTotalEvents()).isEqualTo(1);
        assertThat(savedDashboard.getPublishedEvents()).isEqualTo(0);
        assertThat(savedDashboard.getEventsByCategory().get("MUSIC")).isEqualTo(1);
    }

    @Test
    void processEventNotification_shouldIncrementPublishedEventsIfStatusIsPublished() {
        // Arrange
        payload.setStatus("PUBLISHED");
        when(eventNotificationRepository.existsByEventId("msg-999")).thenReturn(false);
        when(eventNotificationRepository.findByEntityId("event-123")).thenReturn(new ArrayList<>());
        when(cityDashboardRepository.findById("Chicago")).thenReturn(Optional.empty());

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        ArgumentCaptor<CityDashboard> dashboardCaptor = ArgumentCaptor.forClass(CityDashboard.class);
        verify(cityDashboardRepository).save(dashboardCaptor.capture());

        CityDashboard savedDashboard = dashboardCaptor.getValue();
        assertThat(savedDashboard.getTotalEvents()).isEqualTo(1);
        assertThat(savedDashboard.getPublishedEvents()).isEqualTo(1);
    }

    @Test
    void processEventNotification_shouldAdjustCountsOnStatusChange() {
        // Arrange
        EventNotification previousNotification = EventNotification.builder()
                .eventId("msg-888")
                .entityId("event-123")
                .city("Chicago")
                .category("MUSIC")
                .status("DRAFT")
                .processed(true)
                .build();

        payload.setStatus("PUBLISHED");

        CityDashboard existingDashboard = CityDashboard.builder()
                .city("Chicago")
                .totalEvents(1L)
                .publishedEvents(0L)
                .eventsByCategory(new HashMap<>() {{ put("MUSIC", 1L); }})
                .build();

        when(eventNotificationRepository.existsByEventId("msg-999")).thenReturn(false);
        when(eventNotificationRepository.findByEntityId("event-123")).thenReturn(List.of(previousNotification));
        when(cityDashboardRepository.findById("Chicago")).thenReturn(Optional.of(existingDashboard));

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        ArgumentCaptor<CityDashboard> dashboardCaptor = ArgumentCaptor.forClass(CityDashboard.class);
        verify(cityDashboardRepository).save(dashboardCaptor.capture());

        CityDashboard savedDashboard = dashboardCaptor.getValue();
        assertThat(savedDashboard.getTotalEvents()).isEqualTo(1);
        assertThat(savedDashboard.getPublishedEvents()).isEqualTo(1);
        assertThat(savedDashboard.getEventsByCategory().get("MUSIC")).isEqualTo(1);
    }

    @Test
    void processEventNotification_shouldAdjustCountsOnCityChange() {
        // Arrange
        EventNotification previousNotification = EventNotification.builder()
                .eventId("msg-888")
                .entityId("event-123")
                .city("Chicago")
                .category("MUSIC")
                .status("PUBLISHED")
                .processed(true)
                .build();

        payload.setCity("Detroit");
        payload.setStatus("PUBLISHED");

        CityDashboard chicagoDashboard = CityDashboard.builder()
                .city("Chicago")
                .totalEvents(1L)
                .publishedEvents(1L)
                .eventsByCategory(new HashMap<>() {{ put("MUSIC", 1L); }})
                .build();

        CityDashboard detroitDashboard = CityDashboard.builder()
                .city("Detroit")
                .totalEvents(0L)
                .publishedEvents(0L)
                .eventsByCategory(new HashMap<>())
                .build();

        when(eventNotificationRepository.existsByEventId("msg-999")).thenReturn(false);
        when(eventNotificationRepository.findByEntityId("event-123")).thenReturn(List.of(previousNotification));
        when(cityDashboardRepository.findById("Chicago")).thenReturn(Optional.of(chicagoDashboard));
        when(cityDashboardRepository.findById("Detroit")).thenReturn(Optional.of(detroitDashboard));

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        ArgumentCaptor<CityDashboard> dashboardCaptor = ArgumentCaptor.forClass(CityDashboard.class);
        verify(cityDashboardRepository, times(2)).save(dashboardCaptor.capture());

        List<CityDashboard> savedDashboards = dashboardCaptor.getAllValues();
        CityDashboard savedChicago = savedDashboards.stream().filter(d -> d.getCity().equals("Chicago")).findFirst().get();
        CityDashboard savedDetroit = savedDashboards.stream().filter(d -> d.getCity().equals("Detroit")).findFirst().get();

        assertThat(savedChicago.getTotalEvents()).isEqualTo(0);
        assertThat(savedChicago.getPublishedEvents()).isEqualTo(0);
        assertThat(savedChicago.getEventsByCategory().get("MUSIC")).isEqualTo(0);

        assertThat(savedDetroit.getTotalEvents()).isEqualTo(1);
        assertThat(savedDetroit.getPublishedEvents()).isEqualTo(1);
        assertThat(savedDetroit.getEventsByCategory().get("MUSIC")).isEqualTo(1);
    }
}
