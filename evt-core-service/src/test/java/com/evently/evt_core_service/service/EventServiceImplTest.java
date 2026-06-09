package com.evently.evt_core_service.service;

import com.common.evt_commom_util.dto.request.CreateEventRequest;
import com.common.evt_commom_util.dto.request.UpdateStatusRequest;
import com.common.evt_commom_util.dto.EventDTO;
import com.common.evt_commom_util.dto.response.StatsResponse;
import com.common.evt_commom_util.enums.Category;
import com.common.evt_commom_util.enums.Status;
import com.common.evt_commom_util.exception.BadRequestException;
import com.common.evt_commom_util.exception.DuplicateResourceException;
import com.common.evt_commom_util.exception.ResourceNotFoundException;
import com.evently.evt_core_service.entity.Event;
import com.evently.evt_core_service.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private CreateEventRequest createRequest;
    private Event event;

    @BeforeEach
    void setUp() {
        createRequest = CreateEventRequest.builder()
                .eventName("Rock Fest")
                .organizerName("Live Music Inc")
                .organizerMobile("1234567890")
                .city("New York")
                .category(Category.MUSIC)
                .build();

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setEventName("Rock Fest");
        event.setOrganizerName("Live Music Inc");
        event.setOrganizerMobile("1234567890");
        event.setCity("New York");
        event.setCategory(Category.MUSIC);
        event.setStatus(Status.DRAFT);
        event.setCreatedOn(LocalDateTime.now());
        event.setModifiedOn(LocalDateTime.now());
    }

    @Test
    void createEvent_shouldSaveAndReturnEvent() {
        // Arrange
        when(eventRepository.existsByOrganizerMobile("1234567890")).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        EventDTO response = eventService.createEvent(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEventName()).isEqualTo("Rock Fest");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_shouldThrowIfMobileExists() {
        // Arrange
        when(eventRepository.existsByOrganizerMobile("1234567890")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Organizer mobile already registered");
    }

    @Test
    void getEvent_shouldReturnEvent() {
        // Arrange
        UUID id = event.getId();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        // Act
        EventDTO response = eventService.getEvent(id);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
    }

    @Test
    void getEvent_shouldThrowIfNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.getEvent(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void updateStatus_shouldTransitionStatus() {
        // Arrange
        UUID id = event.getId();
        UpdateStatusRequest updateRequest = UpdateStatusRequest.builder().status(Status.PUBLISHED).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        EventDTO response = eventService.updateStatus(id, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateStatus_shouldThrowOnIllegalTransition() {
        // Arrange
        UUID id = event.getId();
        UpdateStatusRequest updateRequest = UpdateStatusRequest.builder().status(Status.CANCELLED).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThatThrownBy(() -> eventService.updateStatus(id, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void listEvents_shouldReturnPaginatedEvents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event));
        when(eventRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<EventDTO> response = eventService.listEvents("New York", Category.MUSIC, Status.DRAFT, pageable);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.getContent().get(0).getEventName()).isEqualTo("Rock Fest");
    }

    @Test
    void getStats_shouldReturnMetrics() {
        // Arrange
        when(eventRepository.count()).thenReturn(10L);
        when(eventRepository.countByStatus()).thenReturn(List.of(new Object[]{Status.DRAFT, 6L}, new Object[]{Status.PUBLISHED, 4L}));
        when(eventRepository.countByCategory()).thenReturn(List.<Object[]>of(new Object[]{Category.MUSIC, 10L}));

        // Act
        StatsResponse stats = eventService.getStats();

        // Assert
        assertThat(stats.getTotalEvents()).isEqualTo(10L);
        assertThat(stats.getByStatus().get("DRAFT")).isEqualTo(6L);
        assertThat(stats.getByStatus().get("PUBLISHED")).isEqualTo(4L);
        assertThat(stats.getByCategory().get("MUSIC")).isEqualTo(10L);
    }
}
