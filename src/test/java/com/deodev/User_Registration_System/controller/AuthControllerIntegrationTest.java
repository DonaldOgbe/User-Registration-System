package com.deodev.User_Registration_System.controller;

import com.deodev.User_Registration_System.dto.request.LoginRequest;
import com.deodev.User_Registration_System.dto.request.RefreshTokenRequest;
import com.deodev.User_Registration_System.dto.request.RegisterRequest;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.VerificationToken;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.repository.VerificationTokenRepository;
import com.deodev.User_Registration_System.service.EmailService;
import com.deodev.User_Registration_System.service.RoleService;
import com.deodev.User_Registration_System.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRole = roleService.getDefaultRole();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully. Please check your email to verify your account."))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());

        assertThat(userRepository.findByEmail("john.doe@example.com")).isPresent();
    }

    @Test
    void testLoginSuccess() throws Exception {
        // given
        User user = User.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("jane.doe@example.com")
                .password(passwordEncoder.encode("password"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .passwordUpdatedAt(new Date())
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("password")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        User user = User.builder()
                .firstname("Peter")
                .lastname("Pan")
                .email("peter.pan@example.com")
                .password(passwordEncoder.encode("password"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(jwtUtil.generateRefreshToken(user.getEmail()));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void verifyToken_whenTokenIsValid_returnsOk() throws Exception {
        // Given
        User user = User.builder()
                .firstname("Test")
                .lastname("User")
                .email("test.user@example.com")
                .password(passwordEncoder.encode("password"))
                .status(UserStatus.PENDING)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        VerificationToken verificationToken = VerificationToken.builder()
                .token("valid-token")
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account activated successfully."));

        assertThat(userRepository.findByEmail("test.user@example.com").get().getStatus())
                .isEqualTo(UserStatus.ACTIVE);
    }
}
