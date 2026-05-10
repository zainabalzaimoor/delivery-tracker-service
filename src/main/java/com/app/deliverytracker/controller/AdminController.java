package com.app.deliverytracker.controller;

import com.app.deliverytracker.model.DriverAssignment;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.repository.OrderRepository;
import com.app.deliverytracker.service.DriverAssignmentService;
import com.app.deliverytracker.service.OrderService;
import com.app.deliverytracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final DriverAssignmentService assignmentService;
    private final OrderService orderService;

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok("User account has been deactivated successfully.");
    }

    @PostMapping("/assignments/{orderId}/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverAssignment> assignDriver(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                assignmentService.assignDriver(orderId, driverId)
        );
    }

    @GetMapping("/orders/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAll(){
        return orderService.getAllOrders();
    }


}
