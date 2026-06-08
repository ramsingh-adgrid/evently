package com.evently.evt_core_service.service;

import com.evently.evt_core_service.dto.request.CreateEventRequest;
import com.evently.evt_core_service.dto.request.UpdateStatusRequest;
import com.evently.evt_core_service.dto.response.EventResponse;
import com.evently.evt_core_service.entity.Event;
import com.evently.evt_core_service.enums.Category;
import com.evently.evt_core_service.enums.Status;
import com.evently.evt_core_service.exception.BadRequestException;
import com.evently.evt_core_service.exception.DuplicateResourceException;
import com.evently.evt_core_service.exception.ResourceNotFoundException;
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
    public EventResponse createEvent(CreateEventRequest request) {
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
    public EventResponse getEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + id));
        return toResponse(event);
    }

    @Override
    public Page<EventResponse> listEvents(String city, Category category,
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
    public EventResponse updateStatus(UUID id, UpdateStatusRequest request) {
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
    public Object getStats() {
        long total = eventRepository.count();
        var byStatus = new java.util.HashMap<String, Long>();
        var byCategory = new java.util.HashMap<String, Long>();

        for (Status status : Status.values()) {
            long count = eventRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("status"), status)
            ).size();
            byStatus.put(status.name(), count);
        }

        for (Category category : Category.values()) {
            long count = eventRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("category"), category)
            ).size();
            byCategory.put(category.name(), count);
        }

        return new java.util.HashMap<String, Object>() {{
            put("totalEvents", total);
            put("byStatus", byStatus);
            put("byCategory", byCategory);
        }};
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

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
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
