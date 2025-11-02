package com.deodev.User_Registration_System.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
public record LoginRequest(
        String email,
        String password
) {
}
