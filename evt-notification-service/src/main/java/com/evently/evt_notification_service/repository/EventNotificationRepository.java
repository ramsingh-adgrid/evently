package com.evently.evt_notification_service.repository;

import com.evently.evt_notification_service.document.EventNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventNotificationRepository extends MongoRepository<EventNotification, String> {
    boolean existsByEventId(String eventId);
    void deleteByEventId(String eventId);
    List<EventNotification> findByEntityId(String entityId);
    Page<EventNotification> findByEntityId(String entityId, Pageable pageable);
    Optional<EventNotification> findFirstByEntityIdOrderByReceivedAtDesc(String entityId);
}
