package com.deodev.User_Registration_System.dto.response;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String error
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, T data, String error) {
        return new ApiResponse<>(false, message, data, error);
    }
}
