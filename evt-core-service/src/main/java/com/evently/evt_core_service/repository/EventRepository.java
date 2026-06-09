package com.evently.evt_core_service.repository;

import com.evently.evt_core_service.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>,
        JpaSpecificationExecutor<Event> {

    boolean existsByOrganizerMobile(String organizerMobile);

    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    List<Object[]> countByStatus();

    @Query("SELECT e.category, COUNT(e) FROM Event e GROUP BY e.category")
    List<Object[]> countByCategory();
}