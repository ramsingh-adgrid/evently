package com.evently.evt_notification_service.repository;

import com.evently.evt_notification_service.document.CityDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityDashboardRepository extends MongoRepository<CityDashboard, String> {
}
