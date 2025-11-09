package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.dto.request.LoginRequest;
import com.deodev.User_Registration_System.dto.request.RefreshTokenRequest;
import com.deodev.User_Registration_System.dto.request.RegisterRequest;
import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.AuthResponse;
import com.deodev.User_Registration_System.exception.ResourceNotFoundException;
import com.deodev.User_Registration_System.exception.VerificationTokenException;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.repository.RoleRepository;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.deodev.User_Registration_System.commons.AppConstants.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final VerificationService verificationService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public ApiResponse<?> register(RegisterRequest registerRequest) {
        try {
            if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
                throw new IllegalArgumentException(EMAIL_ALREADY_EXISTS);
            }
            User user = userService.createNewUser(registerRequest);

            CustomUserDetails userDetails = authenticate(registerRequest.email(), registerRequest.password());
            List<String> authorities = getAuthorities(userDetails);

            Map<String, Object> claims = new HashMap<>();
            claims.put("passwordUpAt", user.getPasswordUpdatedAt());
            claims.put("authorities", authorities);
            claims.put("userId", user.getId());

            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            verificationService.sendVerificationLink(user);

            return ApiResponse.success(USER_REGISTRATION_SUCCESS,
                    AuthResponse.builder()
                            .accessToken(newAccessToken).refreshToken(newRefreshToken).build());
        } catch (Exception ex) {
            log.error("Unexpected error during registration for user: {}, Error: {}", registerRequest.email(), ex.getMessage());
            throw ex;
        }
    }

    public ApiResponse<?> login(LoginRequest loginRequest) {
        try {

            CustomUserDetails userDetails = authenticate(loginRequest.email(), loginRequest.password());

            List<String> authorities = getAuthorities(userDetails);

            Map<String, Object> claims = new HashMap<>();
            User user = userService.findUserById(userDetails.user().getId());
            claims.put("passwordUpAt", user.getPasswordUpdatedAt());
            claims.put("authorities", authorities);
            claims.put("userId", userDetails.user().getId());


            String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername(), claims);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            return ApiResponse.success(USER_LOGIN_SUCCESS,
                    AuthResponse.builder()
                            .accessToken(accessToken).refreshToken(refreshToken).build());
        } catch (Exception ex) {
            log.error("Unexpected error during login for user: {}, Error: {}", loginRequest.email(), ex.getMessage());
            throw ex;
        }

    }

    public ApiResponse<?> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            if (!jwtUtil.validateToken(refreshTokenRequest.refreshToken())) {
                throw new VerificationTokenException(REFRESH_TOKEN_INVALID);
            }

            String userEmail = jwtUtil.getUsernameFromToken(refreshTokenRequest.refreshToken());
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
            List<String> authorities = getAuthoritiesFromRoles(user);


            Map<String, Object> claims = new HashMap<>();
            claims.put("authorities", authorities);
            claims.put("userId", user.getId());

            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            return ApiResponse.success(REFRESH_TOKEN_SUCCESS,
                    AuthResponse.builder()
                            .accessToken(newAccessToken).refreshToken(newRefreshToken).build());
        } catch (Exception ex) {
            log.error("Unexpected error refreshing token, Error: {}", ex.getMessage());
            throw ex;
        }

    }

    CustomUserDetails authenticate(String email, String password) {
        Authentication loginAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        return (CustomUserDetails) loginAuthentication.getPrincipal();
    }

    List<String> getAuthoritiesFromRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .toList();
    }

    List<String> getAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
