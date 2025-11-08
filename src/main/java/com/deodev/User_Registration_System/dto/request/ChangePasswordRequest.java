package com.deodev.User_Registration_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ChangePasswordRequest(
        @NotNull(message = "User ID cannot be null")
        UUID userId,
        @NotBlank(message = "Old password cannot be blank")
        String oldPassword,
        @NotBlank(message = "New password cannot be blank")
        String newPassword,
        @NotBlank(message = "Confirm password cannot be blank")
        String confirmPassword
) {
}
