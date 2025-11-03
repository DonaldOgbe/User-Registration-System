package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.dto.request.LoginRequest;
import com.deodev.User_Registration_System.dto.request.RefreshTokenRequest;
import com.deodev.User_Registration_System.dto.request.RegisterRequest;
import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.AuthResponse;
import com.deodev.User_Registration_System.exception.TokenValidationException;
import com.deodev.User_Registration_System.exception.ResourceNotFoundException;
import com.deodev.User_Registration_System.exception.VerificationTokenException;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.repository.RoleRepository;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final VerificationService verificationService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public ApiResponse<?> register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new IllegalArgumentException(AppConstants.EMAIL_ALREADY_EXISTS);
        }
        User user = userService.createNewUser(registerRequest);
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ROLE_NOT_FOUND));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        userRepository.save(user);
        verificationService.createVerificationToken(user);

        List<String> authorities = getAuthoritiesFromRoles(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", user.getId());

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ApiResponse.success(AppConstants.USER_REGISTRATION_SUCCESS,
                AuthResponse.builder()
                        .accessToken(newAccessToken).refreshToken(newRefreshToken).build());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> authorities = getAuthorities(userDetails);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", userDetails.user().getId());

        String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        if (!jwtUtil.validateToken(refreshTokenRequest.refreshToken())) {
            throw new VerificationTokenException(AppConstants.REFRESH_TOKEN_INVALID);
        }

        String userEmail = jwtUtil.getUsernameFromToken(refreshTokenRequest.refreshToken());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
        List<String> authorities = getAuthoritiesFromRoles(user);


        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", user.getId());

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken);
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
