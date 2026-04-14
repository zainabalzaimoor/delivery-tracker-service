package com.app.deliverytracker.model;

import com.app.deliverytracker.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String pickupAddress;
    private String dropoffAddress;
    private String recipientName;
    private String recipientPhone;
    private Double totalPrice;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    @OneToMany(mappedBy = "order")
    private List<DriverAssignment> driverAssignments;
    @OneToMany(mappedBy = "order")
    private List<LocationUpdate> locationUpdates;
    @OneToMany(mappedBy = "order")
    private List<DeliveryStatusHistory> statusHistory;

}
