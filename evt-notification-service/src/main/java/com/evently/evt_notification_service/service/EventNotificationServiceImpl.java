package com.evently.evt_notification_service.service;

import com.evently.evt_notification_service.document.CityDashboard;
import com.evently.evt_notification_service.document.EventNotification;
import com.evently.evt_notification_service.dto.EventPayload;
import com.evently.evt_notification_service.dto.KafkaEventWrapper;
import com.evently.evt_notification_service.repository.CityDashboardRepository;
import com.evently.evt_notification_service.repository.EventNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationServiceImpl implements EventNotificationService {

    private final EventNotificationRepository eventNotificationRepository;
    private final CityDashboardRepository cityDashboardRepository;

    @Override
    @Transactional
    public void processEventNotification(KafkaEventWrapper wrapper) {
        String eventId = wrapper.getEventId();
        if (eventNotificationRepository.existsByEventId(eventId)) {
            log.warn("Duplicate message received. Event ID {} already exists in database. Skipping processing.", eventId);
            return;
        }

        EventPayload payload = wrapper.getPayload();
        if (payload == null) {
            log.error("Received message with null payload. Event ID: {}", eventId);
            throw new IllegalArgumentException("Payload cannot be null");
        }


        EventNotification notification = EventNotification.builder()
                .eventId(eventId)
                .entityId(payload.getId())
                .eventName(payload.getEventName())
                .eventType(wrapper.getEventType())
                .city(payload.getCity())
                .category(payload.getCategory())
                .status(payload.getStatus())
                .receivedAt(LocalDateTime.now())
                .processed(false)
                .build();

        eventNotificationRepository.save(notification);

        try {

            updateDashboard(payload);


            notification.setProcessed(true);
            eventNotificationRepository.save(notification);
            log.info("Successfully processed event notification. Event ID: {}, Entity ID: {}", eventId, payload.getId());
        } catch (Exception e) {
            log.error("Error processing event notification. Event ID: {}", eventId, e);
            throw e; // rethrow to trigger retry/DLT
        }
    }




    private void updateDashboard(EventPayload payload) {
        String city = payload.getCity();
        if (city == null || city.isBlank()) {
            log.warn("City is empty for event entity ID: {}. Skipping dashboard update.", payload.getId());
            return;
        }

        String category = payload.getCategory();
        String status = payload.getStatus();


        Optional<EventNotification> previousNotificationOpt = eventNotificationRepository
                .findByEntityId(payload.getId()).stream()
                .filter(EventNotification::isProcessed)
                .max((n1, n2) -> n1.getReceivedAt().compareTo(n2.getReceivedAt()));

        if (previousNotificationOpt.isEmpty()) {
//if the event is new   increment the  new count
            incrementDashboardCounts(city, category, status);
        } else {
        // if the event exists previously we have to adjus the counts on the
            //basis of the chnages in the payload
            EventNotification prev = previousNotificationOpt.get();
            adjustDashboardCounts(prev.getCity(), prev.getCategory(), prev.getStatus(), city, category, status);
        }
    }




   //increment the count if the city dashboard exists else we will create new city dashboard and adjust the count
    private void incrementDashboardCounts(String city, String category, String status) {
        CityDashboard dashboard = cityDashboardRepository.findById(city)
                .orElseGet(() -> CityDashboard.builder()
                        .city(city)
                        .totalEvents(0L)
                        .publishedEvents(0L)
                        .eventsByCategory(new HashMap<>())
                        .build());

        dashboard.setTotalEvents(dashboard.getTotalEvents() + 1);
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            dashboard.setPublishedEvents(dashboard.getPublishedEvents() + 1);
        }


        if (category != null && !category.isBlank()) {
            String catUpper = category.toUpperCase();
            long count = dashboard.getEventsByCategory().getOrDefault(catUpper, 0L);
            dashboard.getEventsByCategory().put(catUpper, count + 1);
        }

        dashboard.setLastUpdatedAt(LocalDateTime.now());
        cityDashboardRepository.save(dashboard);
        log.info("Incremented dashboard counts for city={}", city);
    }





  //adjust the dashboard counts if the event city is not changed
    private void adjustDashboardCounts(String oldCity, String oldCategory, String oldStatus,
                                       String newCity, String newCategory, String newStatus) {
        if (!oldCity.equalsIgnoreCase(newCity)) {

            decrementDashboardCounts(oldCity, oldCategory, oldStatus);
            incrementDashboardCounts(newCity, newCategory, newStatus);
            return;
        }


        CityDashboard dashboard = cityDashboardRepository.findById(newCity)
                .orElseGet(() -> CityDashboard.builder()
                        .city(newCity)
                        .totalEvents(0L)
                        .publishedEvents(0L)
                        .eventsByCategory(new HashMap<>())
                        .build());


        if (oldCategory != null && !oldCategory.equalsIgnoreCase(newCategory)) {

            String oldCatUpper = oldCategory.toUpperCase();
            long oldCatCount = dashboard.getEventsByCategory().getOrDefault(oldCatUpper, 0L);
            dashboard.getEventsByCategory().put(oldCatUpper, Math.max(0, oldCatCount - 1));


            if (newCategory != null && !newCategory.isBlank()) {
                String newCatUpper = newCategory.toUpperCase();
                long newCatCount = dashboard.getEventsByCategory().getOrDefault(newCatUpper, 0L);
                dashboard.getEventsByCategory().put(newCatUpper, newCatCount + 1);
            }
        }


        boolean wasPublished = "PUBLISHED".equalsIgnoreCase(oldStatus);
        boolean isPublished = "PUBLISHED".equalsIgnoreCase(newStatus);

        if (wasPublished && !isPublished) {
            dashboard.setPublishedEvents(Math.max(0, dashboard.getPublishedEvents() - 1));
        } else if (!wasPublished && isPublished) {
            dashboard.setPublishedEvents(dashboard.getPublishedEvents() + 1);
        }

        dashboard.setLastUpdatedAt(LocalDateTime.now());
        cityDashboardRepository.save(dashboard);
        log.info("Adjusted dashboard counts for city={}", newCity);
    }






    private void decrementDashboardCounts(String city, String category, String status) {
        Optional<CityDashboard> dashboardOpt = cityDashboardRepository.findById(city);
        if (dashboardOpt.isEmpty()) {
            return;
        }
        CityDashboard dashboard = dashboardOpt.get();
        dashboard.setTotalEvents(Math.max(0, dashboard.getTotalEvents() - 1));

        if ("PUBLISHED".equalsIgnoreCase(status)) {
            dashboard.setPublishedEvents(Math.max(0, dashboard.getPublishedEvents() - 1));
        }

        if (category != null && !category.isBlank()) {
            String catUpper = category.toUpperCase();
            long count = dashboard.getEventsByCategory().getOrDefault(catUpper, 0L);
            dashboard.getEventsByCategory().put(catUpper, Math.max(0, count - 1));
        }

        dashboard.setLastUpdatedAt(LocalDateTime.now());
        cityDashboardRepository.save(dashboard);
        log.info("Decremented dashboard counts for city={}", city);
    }





    @Override
    public List<EventNotification> getNotificationsByEntityId(String entityId) {
        return eventNotificationRepository.findByEntityId(entityId);
    }




    @Override
    public CityDashboard getDashboardByCity(String city) {
        return cityDashboardRepository.findById(city)
                .orElseThrow(() -> new com.evently.evt_notification_service.exception.ResourceNotFoundException(
                        "No dashboard data found for city: " + city));
    }
}
