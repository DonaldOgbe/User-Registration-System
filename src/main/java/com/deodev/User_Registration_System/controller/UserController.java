package com.deodev.User_Registration_System.controller;

import com.deodev.User_Registration_System.dto.request.ChangePasswordRequest;
import com.deodev.User_Registration_System.dto.request.UpdateUserRequest;
import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> getUser(@PathVariable UUID userId) {
        log.info("Received request to fetch user with ID: {}", userId);
        ApiResponse<?> response = userService.getUser(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UpdateUserRequest request) {
        log.info("Received request to update user with ID: {}", request.userId());
        ApiResponse<?> response = userService.updateUser(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequest request) {
        log.info("Received request to change password for user with ID: {}", request.userId());
        ApiResponse<?> response = userService.changePassword(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
