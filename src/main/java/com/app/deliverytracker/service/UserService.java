package com.app.deliverytracker.service;

import com.app.deliverytracker.dto.LoginRequest;
import com.app.deliverytracker.dto.UserProfileUpdateDTO;
import com.app.deliverytracker.enums.Role;
import com.app.deliverytracker.enums.UserStatus;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.model.UserProfile;
import com.app.deliverytracker.repository.UserProfileRepository;
import com.app.deliverytracker.repository.UserRepository;
import com.app.deliverytracker.security.JWTUtils;
import com.app.deliverytracker.security.MyUserDetailsService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, JWTUtils jwtUtils, MyUserDetailsService userDetailsService, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userProfileRepository = userProfileRepository;
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

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email."));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // Token valid for 15 mins

        userRepository.save(user);
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    public boolean isResetTokenValid(String token) {
        return userRepository.findByResetToken(token)
                .map(user -> user.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public void updatePassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token."));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Critical: Token can only be used once
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("The old password you entered is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserProfile(String email, UserProfileUpdateDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getProfile();

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        if (request.phone() != null) profile.setPhone(request.phone());
        if (request.address() != null) profile.setAddress(request.address());
        if (request.dateOfBirth() != null) profile.setDateOfBirth(request.dateOfBirth());

        userProfileRepository.save(profile);
    }
    @Transactional
    public String uploadImage(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = getOrCreateProfile(user);
        String UPLOAD_DIR = "uploads/profiles/";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        profile.setProfileImage(fileName);
        userProfileRepository.save(profile);

        return fileName;
    }

    private UserProfile getOrCreateProfile(User user) {
        if (user.getProfile() == null) {
            UserProfile newProfile = new UserProfile();
            newProfile.setUser(user);
            return newProfile;
        }
        return user.getProfile();
    }



}
