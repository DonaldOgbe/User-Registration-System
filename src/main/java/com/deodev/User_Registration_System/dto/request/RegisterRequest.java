package com.deodev.User_Registration_System.dto.request;

import lombok.Builder;

@Builder
public record RegisterRequest(
        String firstname,
        String lastname,
        String email,
        String password
) {
}
