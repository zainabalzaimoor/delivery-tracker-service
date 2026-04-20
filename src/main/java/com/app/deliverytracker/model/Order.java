package com.app.deliverytracker.model;

import com.app.deliverytracker.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
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
    @JsonBackReference
    private List<DriverAssignment> driverAssignments;
    @OneToMany(mappedBy = "order")
    @JsonBackReference
    private List<LocationUpdate> locationUpdates;
    @OneToMany(mappedBy = "order")
    @JsonBackReference
    private List<DeliveryStatusHistory> statusHistory;

}
