package com.app.deliverytracker.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record UserProfileUpdateDTO(
        String phone,
        String address,
        LocalDate dateOfBirth,
        MultipartFile file
) {}