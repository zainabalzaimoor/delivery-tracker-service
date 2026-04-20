package com.app.deliverytracker.service;

import com.app.deliverytracker.dto.OrderRequest;
import com.app.deliverytracker.enums.OrderStatus;
import com.app.deliverytracker.model.DeliveryStatusHistory;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.repository.DeliveryStatusHistoryRepository;
import com.app.deliverytracker.repository.OrderRepository;
import com.app.deliverytracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;

    // Create Order by a customer
    @Transactional
    public Order createOrder(String email, OrderRequest request){
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setPickupAddress(request.pickupAddress());
        order.setDropoffAddress(request.dropoffAddress());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setTotalPrice(request.totalPrice());
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = orderRepository.save(order);
        saveStatusHistory(savedOrder);

        String subject = "Order Confirmation";
        String emailBody = """
        Hello %s,

        Your order Id #%d has been successfully created and is being processed.

        Best regards,
        Delivery Tracker Team
        """.formatted(customer.getUsername(), order.getId());

        String shortMessage = "Order " + order.getId() + " created successfully.";

        notificationService.notify(order.getCustomer(), subject, emailBody, shortMessage, true);

        return savedOrder;
    }

    private void saveStatusHistory(Order order) {
        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.CREATED);
        history.setUpdatedAt(LocalDateTime.now());

        statusHistoryRepository.save(history);
    }

    // get All Orders -- ADMIN
    public List<Order> getAllOrders(){

        return orderRepository.findAll();
    }

    public List<Order> getOrdersByCustomer(Long customerId){
        return orderRepository.findAll()
                .stream()
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .toList();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public Order updateOrder(Long id, OrderRequest request) {
        Order order = getOrderById(id);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Cannot update order after it has been processed");
        }
        order.setPickupAddress(request.pickupAddress());
        order.setDropoffAddress(request.dropoffAddress());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setTotalPrice(request.totalPrice());
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
        if(order.getStatus() == OrderStatus.ASSIGNED){
            throw new RuntimeException("Cannot cancel an assigned order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        saveStatusHistory(order);
        return orderRepository.save(order);
    }

}
