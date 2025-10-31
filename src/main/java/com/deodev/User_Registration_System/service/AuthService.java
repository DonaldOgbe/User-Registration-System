package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.dto.request.LoginRequest;
import com.deodev.User_Registration_System.dto.request.RegisterRequest;
import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.AuthResponse;
import com.deodev.User_Registration_System.exception.TokenValidationException;
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

    public ApiResponse<?> register(RegisterRequest registerRequest) {
        User user = userService.createNewUser(registerRequest);
        verificationService.createVerificationToken(user);

        List<String> authorities = getAuthoritiesFromRoles(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", user.getId());

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ApiResponse.success("User registered Successfully",
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

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new TokenValidationException("Invalid refresh token");
        }

        String userEmail = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
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
