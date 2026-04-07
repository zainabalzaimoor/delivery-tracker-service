package com.app.deliverytracker.controller;

import com.app.deliverytracker.dto.ChangePasswordRequest;
import com.app.deliverytracker.dto.LoginRequest;
import com.app.deliverytracker.dto.ResetPasswordRequest;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        System.out.println("Calling loginUser ==> ");
        try {
            String token = userService.loginUser(request);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}
