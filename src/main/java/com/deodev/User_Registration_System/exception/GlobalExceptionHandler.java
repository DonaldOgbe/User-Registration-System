package com.deodev.User_Registration_System.exception;

import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTokenValidationException(TokenValidationException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(ApiResponse.error(ex.getLocalizedMessage(), errorResponse, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationTokenException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleVerificationTokenException(VerificationTokenException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(ApiResponse.error(ex.getLocalizedMessage(), errorResponse, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
