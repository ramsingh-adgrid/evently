package com.evently.evt_notification_service.repository;

import com.evently.evt_notification_service.document.EventNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventNotificationRepository extends MongoRepository<EventNotification, String> {
    boolean existsByEventId(String eventId);
    List<EventNotification> findByEntityId(String entityId);
    Optional<EventNotification> findFirstByEntityIdOrderByReceivedAtDesc(String entityId);
}
