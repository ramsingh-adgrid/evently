package com.evently.evt_notification_service.service;

import com.common.evt_commom_util.dto.response.EventResponse;
import com.common.evt_commom_util.dto.kafka.KafkaEventWrapper;
import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
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
import java.util.UUID;

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
    private EventResponse payload;

    @BeforeEach
    void setUp() {
        payload = EventResponse.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000123"))
                .eventName("Salsa Night")
                .city("Chicago")
                .category(com.common.evt_commom_util.enums.Category.MUSIC)
                .status(com.common.evt_commom_util.enums.Status.DRAFT)
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
        when(eventNotificationRepository.save(any(EventNotification.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate eventId"));

        // Act
        eventNotificationService.processEventNotification(wrapper);

        // Assert
        verify(eventNotificationRepository, times(1)).save(any(EventNotification.class));
        verify(cityDashboardRepository, never()).save(any());
    }

    @Test
    void processEventNotification_shouldCreateNewDashboardIfFirstEvent() {
        // Arrange
        when(eventNotificationRepository.findByEntityId("00000000-0000-0000-0000-000000000123")).thenReturn(new ArrayList<>());
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
        payload.setStatus(com.common.evt_commom_util.enums.Status.PUBLISHED);
        when(eventNotificationRepository.findByEntityId("00000000-0000-0000-0000-000000000123")).thenReturn(new ArrayList<>());
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
                .entityId("00000000-0000-0000-0000-000000000123")
                .city("Chicago")
                .category("MUSIC")
                .status("DRAFT")
                .processed(true)
                .build();

        payload.setStatus(com.common.evt_commom_util.enums.Status.PUBLISHED);

        CityDashboard existingDashboard = CityDashboard.builder()
                .city("Chicago")
                .totalEvents(1L)
                .publishedEvents(0L)
                .eventsByCategory(new HashMap<>() {{ put("MUSIC", 1L); }})
                .build();

        when(eventNotificationRepository.findByEntityId("00000000-0000-0000-0000-000000000123")).thenReturn(List.of(previousNotification));
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
                .entityId("00000000-0000-0000-0000-000000000123")
                .city("Chicago")
                .category("MUSIC")
                .status("PUBLISHED")
                .processed(true)
                .build();

        payload.setCity("Detroit");
        payload.setStatus(com.common.evt_commom_util.enums.Status.PUBLISHED);

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

        when(eventNotificationRepository.findByEntityId("00000000-0000-0000-0000-000000000123")).thenReturn(List.of(previousNotification));
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

    @Test
    void getNotificationsByEntityId_shouldReturnPaginatedNotifications() {
        // Arrange
        String entityId = "event-123";
        EventNotification notification = EventNotification.builder().entityId(entityId).build();
        org.springframework.data.domain.Page<EventNotification> pageResult = new org.springframework.data.domain.PageImpl<>(List.of(notification));
        
        when(eventNotificationRepository.findByEntityId(eq(entityId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageResult);

        // Act
        List<EventNotification> results = eventNotificationService.getNotificationsByEntityId(entityId, 0, 10);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEntityId()).isEqualTo(entityId);
    }

    @Test
    void getDashboardByCity_shouldReturnDashboard() {
        // Arrange
        String city = "Chicago";
        CityDashboard dashboard = CityDashboard.builder().city(city).build();
        when(cityDashboardRepository.findById(city)).thenReturn(Optional.of(dashboard));

        // Act
        CityDashboard result = eventNotificationService.getDashboardByCity(city);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo(city);
    }

    @Test
    void getDashboardByCity_shouldThrowIfNotFound() {
        // Arrange
        String city = "Chicago";
        when(cityDashboardRepository.findById(city)).thenReturn(Optional.empty());

        // Act & Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventNotificationService.getDashboardByCity(city))
                .isInstanceOf(com.evently.evt_notification_service.exception.ResourceNotFoundException.class);
    }
}
