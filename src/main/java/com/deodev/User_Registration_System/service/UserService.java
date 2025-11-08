package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.dto.request.ChangePasswordRequest;
import com.deodev.User_Registration_System.dto.request.RegisterRequest;
import com.deodev.User_Registration_System.dto.request.UpdateUserRequest;
import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.UserDetailsResponse;
import com.deodev.User_Registration_System.exception.InvalidPasswordException;
import com.deodev.User_Registration_System.exception.ResourceNotFoundException;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createNewUser(RegisterRequest registerRequest) {
        User user = User.builder()
                .firstname(registerRequest.firstname())
                .lastname(registerRequest.lastname())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .status(UserStatus.PENDING)
                .roles(Set.of(roleService.getDefaultRole()))
                .build();
        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public ApiResponse<?> getUser(UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));

        UserDetailsResponse userDetails = UserDetailsResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .build();

        log.info("User fetched successfully with ID: {}", userId);
        return ApiResponse.success(AppConstants.USER_FETCH_SUCCESS, userDetails);
    }

    @Transactional
    public ApiResponse<?> updateUser(UpdateUserRequest request) {
        log.info("Updating user with ID: {}", request.userId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));

        user.setFirstname(request.firstName());
        user.setLastname(request.lastName());
        User updatedUser = userRepository.save(user);

        UserDetailsResponse userDetails = UserDetailsResponse.builder()
                .userId(updatedUser.getId())
                .email(updatedUser.getEmail())
                .firstName(updatedUser.getFirstname())
                .lastName(updatedUser.getLastname())
                .build();

        log.info("User updated successfully with ID: {}", request.userId());
        return ApiResponse.success(AppConstants.USER_UPDATE_SUCCESS, userDetails);
    }

    @Transactional
    public ApiResponse<Void> changePassword(ChangePasswordRequest request) {
        log.info("Attempting to change password for user with ID: {}", request.userId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));

        verifyOldPassword(request.oldPassword(), user.getPassword());
        verifyNewPasswordMatch(request.newPassword(), request.confirmPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("Password changed successfully for user with ID: {}", request.userId());
        return ApiResponse.success(AppConstants.PASSWORD_CHANGE_SUCCESS, null);
    }

    private void verifyOldPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.warn("Old password verification failed.");
            throw new InvalidPasswordException(AppConstants.INVALID_OLD_PASSWORD);
        }
    }

    private void verifyNewPasswordMatch(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            log.warn("New password and confirm password do not match.");
            throw new InvalidPasswordException(AppConstants.PASSWORD_MISMATCH);
        }
    }
}
