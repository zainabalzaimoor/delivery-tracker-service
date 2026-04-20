package com.app.deliverytracker.repository;

import com.app.deliverytracker.model.LocationUpdate;
import com.app.deliverytracker.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, Long> {
   Optional <LocationUpdate> findTopByOrderOrderByTimestampDesc(Order order);

}
