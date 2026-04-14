package com.app.deliverytracker.dto;

import java.time.LocalDate;

public record UserProfileUpdateDTO(
        String phone,
        String address,
        LocalDate dateOfBirth
) {}