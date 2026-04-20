package com.app.deliverytracker.service;

import com.app.deliverytracker.model.Notification;
import com.app.deliverytracker.model.Order;
import com.app.deliverytracker.model.User;
import com.app.deliverytracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void notify(User user, String subject, String emailBody, String shortMessage, boolean sendEmail) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(shortMessage);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        if (sendEmail && user.getEmail() != null) {
            emailService.sendEmail(
                    user.getEmail(),
                    subject,
                    emailBody
            );
        }
    }
}
