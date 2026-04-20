package com.app.deliverytracker.controller;

import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.service.DriverAssignmentService;
import com.app.deliverytracker.service.LocationUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driver")
public class DriverController {
    private final DriverAssignmentService service;
    private final LocationUpdateService locationUpdateService;

    @PutMapping("/start/{orderId}/{driverId}")
    public ResponseEntity<Order> startDelivery(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {

        return ResponseEntity.ok(service.startDelivery(orderId, driverId));
    }

    @PutMapping("/complete/{orderId}/{driverId}")
    public ResponseEntity<Order> completeDelivery(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {

        return ResponseEntity.ok(service.completeDelivery(orderId, driverId));
    }

    // sends locations
    @PostMapping("/locations/{orderId}/{driverId}")
    public String update(
            @PathVariable Long orderId,
            @PathVariable Long driverId,
            @RequestParam Double lat,
            @RequestParam Double lng){
        locationUpdateService.updateLocation(orderId,driverId,lat,lng);

        return "Location sent";
    }

}
