package com.deodev.User_Registration_System.config;

import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.service.RoleService;
import com.deodev.User_Registration_System.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.deodev.User_Registration_System.commons.AppConstants.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtil mockJwtUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRole = roleService.getDefaultRole();
    }

    @Test
    void
    request_ShouldReturn401_WhenTokenIsInvalid() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/some-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer badToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(FAILED_AUTHORIZATION))
                .andExpect(jsonPath("$.data.error").value(ACCESS_TOKEN_INVALID));
    }

    @Test
    void request_ShouldReturn500_WhenUnexpectedExceptionOccurs() throws Exception {
        // given
        when(mockJwtUtil.validateToken("boomToken")).thenThrow(new RuntimeException("Unexpected"));

        // when & then
        mockMvc.perform(get("/api/v1/users/some-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer boomToken"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(FAILED_AUTHORIZATION))
                .andExpect(jsonPath("$.data.error").value(INTERNAL_SERVER_ERROR));
    }

    @Test
    void request_ShouldReturn401_WhenPasswordUpdateAtIsAhead() throws Exception {
        // given
        User user = User.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("jane.doe@example.com")
                .password(passwordEncoder.encode("password"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        // Token issued now
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "authorities", List.of("ROLE_USER"),
                "passwordUpAt", new Date()
        );

        String jwt = jwtUtil.generateAccessToken(user.getEmail(), claims);

        Date passwordUpdatedAt = Date.from(Instant.now().plus(2, ChronoUnit.MINUTES));
        user.setPasswordUpdatedAt(passwordUpdatedAt);
        userRepository.save(user);
        

        // when & then
        mockMvc.perform(get("/api/v1/users/" + user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(FAILED_AUTHORIZATION))
                .andExpect(jsonPath("$.data.error").value(ACCESS_TOKEN_INVALID));
    }


}