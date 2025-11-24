package com.deodev.User_Registration_System.config;

import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.ErrorResponse;
import com.deodev.User_Registration_System.exception.TokenValidationException;
import com.deodev.User_Registration_System.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

import static com.deodev.User_Registration_System.commons.AppConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractToken(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            validateAndAuthenticate(jwt, request);
            filterChain.doFilter(request, response);
        } catch (TokenValidationException e) {
            handleTokenError(request, response, e);
            return;
        } catch (Exception e) {
            handleUnexpectedError(request, response, e);
            return;
        }
    }

    String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    void validateAndAuthenticate(String jwt, HttpServletRequest request) {
        if (!jwtUtil.validateToken(jwt)) {
            log.error("Invalid or Expired token, URI: [{}]", request.getRequestURI());
            throw new TokenValidationException(ACCESS_TOKEN_INVALID);
        }

        verifyUsernameAndSetAuthentication(jwt);
        setUserIdAttribute(jwt, request);
    }

    void verifyUsernameAndSetAuthentication(String jwt) {
        String username = jwtUtil.getUsernameFromToken(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(jwt));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        verifyPasswordUpdatedAt(jwt, userDetails);
    }

    void verifyPasswordUpdatedAt(String jwt, CustomUserDetails userDetails) {
        Date passwordUpAt = userDetails.getPasswordUpdatedAt();
        Date iat = jwtUtil.getClaimFromToken(jwt, Claims::getIssuedAt);

        if (iat.before(passwordUpAt)) {
            log.error("Token Expired, Error: Issued at [{}] is before Password update at [{}]", iat, passwordUpAt);
            throw new TokenValidationException(ACCESS_TOKEN_INVALID);
        }
    }

    void setUserIdAttribute(String jwt, HttpServletRequest request) {
        Object userId = jwtUtil.getClaimFromToken(jwt, claims -> (String) claims.get("userId"));

        if (userId != null) {
            request.setAttribute("userId", String.valueOf(userId));
        }
    }

    void handleTokenError(HttpServletRequest request, HttpServletResponse response, TokenValidationException e) throws IOException {
        SecurityContextHolder.clearContext();
        log.warn("Token validation failed for [{} {}]: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ACCESS_TOKEN_INVALID,
                FAILED_AUTHORIZATION, request.getRequestURI());
    }

    void handleUnexpectedError(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        SecurityContextHolder.clearContext();
        log.error("Unexpected error in auth filter for {} {}", request.getMethod(), request.getRequestURI(), e);

        writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR,
                FAILED_AUTHORIZATION, request.getRequestURI());
    }

    void writeErrorResponse(HttpServletResponse response,
                            HttpStatus status,
                            String error,
                            String message,
                            String path) throws IOException {

        ErrorResponse data = ErrorResponse.builder()
                .error(error)
                .path(path)
                .build();

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(message, data);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
        response.getOutputStream().flush();
    }
}
