package com.app.deliverytracker.repository;

import com.app.deliverytracker.model.DriverAssignment;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {
    boolean existsByOrderAndIsActiveTrue(Order order);
    List<DriverAssignment> findByOrder(Order order);
    boolean existsByDriverAndIsActiveTrue(User driver);
}
