package com.app.deliverytracker.service;

import com.app.deliverytracker.controller.LocationSseController;
import com.app.deliverytracker.enums.OrderStatus;
import com.app.deliverytracker.enums.Role;
import com.app.deliverytracker.model.DeliveryStatusHistory;
import com.app.deliverytracker.model.DriverAssignment;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.repository.DeliveryStatusHistoryRepository;
import com.app.deliverytracker.repository.DriverAssignmentRepository;
import com.app.deliverytracker.repository.OrderRepository;
import com.app.deliverytracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    private final DriverAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final LocationSseController sseController;
    private final NotificationService notificationService;
    private final ReentrantLock lock = new ReentrantLock();

    // ASSIGN DRIVER WITH CONCURRENCY CONTROL -- ADMIN
    @Transactional
    public DriverAssignment assignDriver(Long orderId, Long driverId) {

        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " ENTER");

            Thread.sleep(2000); // simulate concurrency

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            User driver = userRepository.findById(driverId)
                    .filter(u -> u.getRole() == Role.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found or invalid role"));

            if (assignmentRepository.existsByOrderAndIsActiveTrue(order)) {
                throw new RuntimeException("Order already assigned");
            }

            boolean driverBusy = assignmentRepository.existsByDriverAndIsActiveTrue(driver);
            if(driverBusy){
                throw new RuntimeException("Driver already has an active order");
            }

            DriverAssignment assignment = new DriverAssignment();
            assignment.setOrder(order);
            assignment.setDriver(driver);
            assignment.setActive(true);
            assignment.setAssignedAt(LocalDateTime.now());

            order.setStatus(OrderStatus.ASSIGNED);
            orderRepository.save(order);

            saveHistory(order, OrderStatus.ASSIGNED);

            System.out.println(Thread.currentThread().getName() + " SUCCESS");

            return assignmentRepository.save(assignment);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // best practice
            throw new RuntimeException("Thread interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    // START DELIVERY -- DRIVER
    @Transactional
    public Order startDelivery(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));


        DriverAssignment assignment = assignmentRepository.findByOrder(order)
                .stream()
                .filter(a -> a.getDriver().getId().equals(driverId) && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Driver not assigned to this order"));


        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new RuntimeException("Order is not ready to start delivery");
        }

        order.setStatus(OrderStatus.ON_THE_WAY);
        orderRepository.save(order);

        User customer = orderRepository.findCustomerByOrderId(orderId);
        String subject = "Delivery Started";
        String emailBody = """
        Hello %s,

        Your order Id #%d is now on the way! A driver has been assigned.

        Best regards,
        Delivery Tracker Team
        """.formatted(customer.getUsername(), order.getId());

        String shortMessage = "Order " + order.getId() + " is on the way.";

        notificationService.notify(order.getCustomer(), subject, emailBody, shortMessage, true);

        saveHistory(order, OrderStatus.ON_THE_WAY);

        return order;
    }

    // COMPLETE DELIVERY -- DRIVER
    @Transactional
    public Order completeDelivery(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        DriverAssignment assignment = assignmentRepository.findByOrder(order)
                .stream()
                .filter(a -> a.getDriver().getId().equals(driverId) && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Driver not assigned to this order"));

        if (order.getStatus() != OrderStatus.ON_THE_WAY) {
            throw new RuntimeException("Order is not in delivery");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        assignment.setActive(false);
        assignmentRepository.save(assignment);


        sseController.closeStream(orderId, "Order DELIVERED!");

        User customer = orderRepository.findCustomerByOrderId(orderId);
        String subject = "Order Delivered";
        String emailBody = """
        Hello %s,

        Your order Id #%d has been delivered. Thank you for choosing us!

        Best regards,
        Delivery Tracker Team
        """.formatted(customer.getUsername(), order.getId());

        String shortMessage = "Order " + order.getId() + " delivered.";

        notificationService.notify(order.getCustomer(), subject, emailBody, shortMessage, true);

        saveHistory(order, OrderStatus.DELIVERED);


        return order;
    }

    private void saveHistory(Order order, OrderStatus status) {
        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setUpdatedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

}
