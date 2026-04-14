package com.app.deliverytracker.service;

import com.app.deliverytracker.dto.LoginRequest;
import com.app.deliverytracker.enums.Role;
import com.app.deliverytracker.enums.UserStatus;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.model.UserProfile;
import com.app.deliverytracker.repository.UserRepository;
import com.app.deliverytracker.security.JWTUtils;
import com.app.deliverytracker.security.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JWTUtils jwtUtils;
    private final MyUserDetailsService userDetailsService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, JWTUtils jwtUtils, MyUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    public ResponseEntity<?> createUser(User user) throws Exception {
        System.out.println("Service Calling CreateUser ==> ");
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception(
                    "User with email " + user.getEmail() + " already exists"
            );
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.INACTIVE);

        UserProfile profile = new UserProfile();
        profile.setUser(user); // link profile to user
        user.setProfile(profile);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerified(false);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        return ResponseEntity.ok(Map.of(
                "status", user.getStatus(),
                "message", "User registered successfully. Please verify your email before logging in."));
    }

    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        user.setVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
    }

    // Login
    public String loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        return jwtUtils.generateToken(userDetails);
    }

}
