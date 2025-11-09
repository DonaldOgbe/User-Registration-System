package com.deodev.User_Registration_System.dto.response;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<ErrorResponse> error(String message, ErrorResponse data) {
        return new ApiResponse<>(false, message, data);
    }
}
