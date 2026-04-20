package com.app.deliverytracker.repository;

import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o.customer FROM Order o WHERE o.id = :orderId")
    User findCustomerByOrderId(@Param("orderId") Long orderId);
}
