package com.app.deliverytracker.controller;

import com.app.deliverytracker.dto.OrderRequest;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request, Principal principal) {
        Order newOrder = orderService.createOrder(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @GetMapping("/getAll")
    public List<Order> getAll(){
        return orderService.getAllOrders();
    }

    //  Get by customer
    @GetMapping("/my-orders/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    // Get by id
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    // Update
    @PutMapping("/update/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequest order) {

        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    // Cancel
    @DeleteMapping("/{id}")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
