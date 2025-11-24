package com.deodev.User_Registration_System.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId
) {
}
