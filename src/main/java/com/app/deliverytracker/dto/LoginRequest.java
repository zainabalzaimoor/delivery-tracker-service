package com.app.deliverytracker.dto;

public record LoginRequest(
        String email,
        String password
) {}