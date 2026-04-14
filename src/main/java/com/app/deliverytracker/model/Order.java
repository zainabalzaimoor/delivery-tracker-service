package com.app.deliverytracker.model;

import com.app.deliverytracker.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String pickupAddress;
    private String dropoffAddress;
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
