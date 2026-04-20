package com.app.deliverytracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"order", "driver"})
@Table(name = "driver_assignments")
public class DriverAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime assignedAt;
    private boolean isActive;
    @Version
    private Integer version; // Optimistic Locking for data integrity


    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;
}
