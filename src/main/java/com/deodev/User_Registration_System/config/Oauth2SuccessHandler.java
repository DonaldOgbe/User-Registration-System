package com.deodev.User_Registration_System.config;

import com.deodev.User_Registration_System.dto.response.ApiResponse;
import com.deodev.User_Registration_System.dto.response.AuthResponse;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.service.UserService;
import com.deodev.User_Registration_System.service.VerificationService;
import com.deodev.User_Registration_System.service.helper.AuthServiceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.deodev.User_Registration_System.commons.AppConstants.USER_LOGIN_SUCCESS;
import static com.deodev.User_Registration_System.commons.AppConstants.USER_REGISTRATION_SUCCESS;


@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final VerificationService verificationService;
    private final AuthServiceHelper authServiceHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();

        ApiResponse<?> apiResponse = userService.existsByEmail(oidcUser.getEmail())
                ? loginOauthUser(oidcUser)
                : registerOauthUser(oidcUser);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private ApiResponse<?> registerOauthUser(OidcUser oidcUser) {
        try {
            User user = userService.createNewOauthUser(oidcUser);

            List<String> authorities = authServiceHelper.getAuthoritiesFromRoles(user);

            Map<String, String> tokens = authServiceHelper.generateTokens(authorities, user);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            verificationService.sendVerificationLink(user);

            return ApiResponse.success(USER_REGISTRATION_SUCCESS,
                    AuthResponse.builder()
                            .accessToken(accessToken).refreshToken(refreshToken).userId(user.getId()).build());
        } catch (Exception ex) {
            log.error("Unexpected error during registration for oauth2 user: {}, Error: {}", oidcUser.getEmail(), ex.getMessage());
            throw ex;
        }
    }

    private ApiResponse<?> loginOauthUser(OidcUser oidcUser) {
        try {
            User user = userService.findUserByEmail(oidcUser.getEmail());
            List<String> authorities = authServiceHelper.getAuthoritiesFromRoles(user);

            Map<String, String> tokens = authServiceHelper.generateTokens(authorities, user);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            return ApiResponse.success(USER_LOGIN_SUCCESS,
                    AuthResponse.builder()
                            .accessToken(accessToken).refreshToken(refreshToken).userId(user.getId()).build());
        } catch (Exception ex) {
            log.error("Unexpected error during login for oauth user: {}, Error: {}", oidcUser.getEmail(), ex.getMessage());
            throw ex;
        }
    }
}
