package com.app.deliverytracker.model;

import com.app.deliverytracker.enums.Role;
import com.app.deliverytracker.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
   @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private boolean isVerified = false;
    @Column(unique = true)
    private String verificationToken;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private UserProfile profile;
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Order> orders;
    @OneToMany(mappedBy = "driver")
    private List<DriverAssignment> driverAssignments;
    @OneToMany(mappedBy = "driver")
    private List<LocationUpdate> locationUpdates;
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

}
