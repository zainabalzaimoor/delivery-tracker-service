package com.app.deliverytracker.controller;

import com.app.deliverytracker.dto.*;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.model.UserProfile;
import com.app.deliverytracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) throws Exception {
        System.out.println("Calling registerUser ==> ");
        return userService.createUser(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        try {
            userService.verifyUser(token);
            return ResponseEntity.ok("Account verified successfully! You can now login.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            userService.processForgotPassword(email);
        } catch (Exception e) {
            // We log the error internally, but tell the user the same thing
            System.out.println("Reset attempted for non-existent email: " + email);
        }
        // The user sees this either way
        return ResponseEntity.ok("If an account exists with that email, a reset link has been sent.");
    }

    // This shows the "Friendly" Page
    @GetMapping("/reset-password")
    public String showResetPage(@RequestParam("token") String token, Model model) {
        if (userService.isResetTokenValid(token)) {
            model.addAttribute("token", token); // Pass token to the HTML
            return "reset-password"; // Points to reset-password.html
        }
        return "error-page"; // Create a simple error.html for expired links
    }

    @PostMapping("/reset-password-ui")
    public String handleHtmlReset(@RequestParam("token") String token,
                                  @RequestParam("newPassword") String newPassword) {
        userService.updatePassword(token, newPassword);
        return "redirect:/api/auth/login-success"; // Redirect to a "Success" page
    }
    @GetMapping("/login-success")
    public String showSuccess() {
        return "success-page";
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails){
        if (!request.newPassword().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body("New passwords do not match.");
        }

        try {
            userService.changePassword(userDetails.getUsername(),
                    request.oldPassword(),
                    request.newPassword());
            return ResponseEntity.ok("Password changed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute UserProfileUpdateDTO userProfileUpdateRequest,
                                           Principal principal) throws IOException {
        UserProfile updated = userService.updateFullProfile(principal.getName(), userProfileUpdateRequest);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/profile/image")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String fileName = userService.uploadImage(userDetails.getUsername(), file);
            return ResponseEntity.ok("Profile image uploaded: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }


}
