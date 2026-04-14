package com.app.deliverytracker.dto;

public record ChangePasswordRequest (String oldPassword,
                                     String newPassword,
                                     String confirmPassword){
}
