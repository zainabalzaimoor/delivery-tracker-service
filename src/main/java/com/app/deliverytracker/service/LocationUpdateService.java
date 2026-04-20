package com.app.deliverytracker.service;

import com.app.deliverytracker.controller.LocationSseController;
import com.app.deliverytracker.repository.DriverAssignmentRepository;
import com.app.deliverytracker.repository.LocationUpdateRepository;
import com.app.deliverytracker.repository.OrderRepository;
import com.app.deliverytracker.repository.UserRepository;
import com.app.deliverytracker.model.LocationUpdate;
import com.app.deliverytracker.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class LocationUpdateService {

    private final LocationUpdateRepository locationRepo;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DriverAssignmentRepository assignmentRepository;
    private final DriverAssignmentService assignmentService;
    private final LocationSseController sseController;

    @Transactional
    public void updateLocation(Long orderId, Long driverId, Double lat, Double lng) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        assignmentRepository.findByOrder(order)
                .stream()
                .filter(a -> a.getDriver().getId().equals(driverId) && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Driver not assigned"));

        LocationUpdate loc = new LocationUpdate();
        loc.setOrder(order);
        loc.setDriver(driver);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setTimestamp(LocalDateTime.now());

        locationRepo.save(loc);

        sseController.sendLocationUpdate(orderId,loc);

        if (isNearDestination(lat, lng)) {
            assignmentService.completeDelivery(orderId, driverId);
        }

    }

    private boolean isNearDestination(Double lat, Double lng) {
        return lat > 90 && lng > 90; // simple simulation
    }

    // Get latest location
    public LocationUpdate getLatestLocation(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return locationRepo.findTopByOrderOrderByTimestampDesc(order)
                .orElseThrow(() -> new RuntimeException("No location yet"));
    }

}