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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public EventDTO createEvent(CreateEventRequest request) {
        if (eventRepository.existsByOrganizerMobile(request.getOrganizerMobile())) {
            throw new DuplicateResourceException(
                    "Organizer mobile already registered: " + request.getOrganizerMobile());
        }
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setOrganizerName(request.getOrganizerName());
        event.setOrganizerMobile(request.getOrganizerMobile());
        event.setCity(request.getCity());
        event.setCategory(request.getCategory());
        Event saved = eventRepository.save(event);
        log.info("Event created eventId={} city={}", saved.getId(), saved.getCity());
        return toResponse(saved);
    }

    @Override
    public EventDTO getEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + id));
        return toResponse(event);
    }

    @Override
    public Page<EventDTO> listEvents(String city, Category category,
                                           Status status, Pageable pageable) {
        List<Specification<Event>> specs = new ArrayList<>();

        if (city != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("city"), city));
        }
        if (category != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Specification<Event> finalSpec = specs.isEmpty()
                ? Specification.allOf()
                : Specification.allOf(specs);

        return eventRepository.findAll(finalSpec, pageable).map(this::toResponse);
    }

    @Override
    public EventDTO updateStatus(UUID id, UpdateStatusRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + id));
        validateTransition(event.getStatus(), request.getStatus());
        event.setStatus(request.getStatus());
        Event saved = eventRepository.save(event);
        log.info("Event status updated eventId={} status={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Override
    public StatsResponse getStats() {
        long total = eventRepository.count();

        var byStatus = new java.util.HashMap<String, Long>();
        for (Status s : Status.values()) {
            byStatus.put(s.name(), 0L);
        }
        for (Object[] row : eventRepository.countByStatus()) {
            Status s = (Status) row[0];
            Long count = (Long) row[1];
            if (s != null) {
                byStatus.put(s.name(), count);
            }
        }

        var byCategory = new java.util.HashMap<String, Long>();
        for (Category c : Category.values()) {
            byCategory.put(c.name(), 0L);
        }
        for (Object[] row : eventRepository.countByCategory()) {
            Category c = (Category) row[0];
            Long count = (Long) row[1];
            if (c != null) {
                byCategory.put(c.name(), count);
            }
        }

        return StatsResponse.builder()
                .totalEvents(total)
                .byStatus(byStatus)
                .byCategory(byCategory)
                .build();
    }

    private void validateTransition(Status current, Status next) {
        boolean valid = switch (current) {
            case DRAFT -> next == Status.PUBLISHED;
            case PUBLISHED -> next == Status.CANCELLED
                    || next == Status.SOLD_OUT;
            default -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " -> " + next);
        }
    }

    private EventDTO toResponse(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .organizerName(event.getOrganizerName())
                .organizerMobile(event.getOrganizerMobile())
                .city(event.getCity())
                .category(event.getCategory())
                .status(event.getStatus())
                .createdOn(event.getCreatedOn())
                .modifiedOn(event.getModifiedOn())
                .build();
    }
}
