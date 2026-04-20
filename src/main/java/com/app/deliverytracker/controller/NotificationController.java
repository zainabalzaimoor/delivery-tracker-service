package com.app.deliverytracker.controller;

import com.app.deliverytracker.model.Notification;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/my")
    public List<Notification> getMyNotifications(@AuthenticationPrincipal User user) {
        return notificationRepository.findByUserId(user.getId());
    }
}