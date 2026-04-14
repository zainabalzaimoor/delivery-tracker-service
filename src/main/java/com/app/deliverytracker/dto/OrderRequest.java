package com.app.deliverytracker.dto;

public record OrderRequest(String pickupAddress,
                           String dropoffAddress,
                           String recipientName,
                           String recipientPhone,
                           double totalPrice) {
}
