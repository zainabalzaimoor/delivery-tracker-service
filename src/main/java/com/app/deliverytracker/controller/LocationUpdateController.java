package com.app.deliverytracker.controller;

import com.app.deliverytracker.model.LocationUpdate;
import com.app.deliverytracker.service.LocationUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationUpdateController {

    private final LocationUpdateService locationService;

    // fetches the latest location for an order -- Customer
    @GetMapping("/{orderId}/latest-location")
    public ResponseEntity<LocationUpdate> getLatestLocation(@PathVariable Long orderId) {
        return ResponseEntity.ok(locationService.getLatestLocation(orderId));
    }

}