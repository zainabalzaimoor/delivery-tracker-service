package com.app.deliverytracker.repository;

import com.app.deliverytracker.model.DeliveryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {
}
