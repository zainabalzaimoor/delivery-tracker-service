package com.app.deliverytracker.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
