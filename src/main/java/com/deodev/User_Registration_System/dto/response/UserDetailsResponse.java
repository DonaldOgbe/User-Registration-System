package com.deodev.User_Registration_System.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserDetailsResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName
) {
}
