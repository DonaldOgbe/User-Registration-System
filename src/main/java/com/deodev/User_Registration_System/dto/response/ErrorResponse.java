package com.deodev.User_Registration_System.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String error,
        String path
) {
}
