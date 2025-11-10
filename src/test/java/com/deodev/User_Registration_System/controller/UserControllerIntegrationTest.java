package com.deodev.User_Registration_System.controller;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.dto.request.ChangePasswordRequest;
import com.deodev.User_Registration_System.dto.request.UpdateUserRequest;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.service.RoleService;
import com.deodev.User_Registration_System.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User createAndSaveUser(String email) {
        Role userRole = roleService.getDefaultRole();
        User user = User.builder()
                .firstname("Test")
                .lastname("User")
                .email(email)
                .password(passwordEncoder.encode("password"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .passwordUpdatedAt(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .build();
        return userRepository.save(user);
    }

    private String generateToken(User user) {
        Map<String, Object> claims = Map.of(
                "passwordUpAt", user.getPasswordUpdatedAt(),
                "authorities", List.of("USER"),
                "userId", user.getId()
        );
        return jwtUtil.generateAccessToken(user.getEmail(), claims);
    }


    @Test
    void getUser_shouldReturnUserDetails_whenAuthenticated() throws Exception {
        // given
        User user = createAndSaveUser("getuser@example.com");
        String accessToken = generateToken(user);
        UUID userId = user.getId();

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(AppConstants.USER_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstname()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastname()));
    }

    @Test
    void updateUser_shouldUpdateDetailsSuccessfully() throws Exception {
        // given
        User user = createAndSaveUser("upadteuser@example.com");
        String accessToken = generateToken(user);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .userId(user.getId())
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .build();

        // when & then
        mockMvc.perform(put("/api/v1/users/update")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(AppConstants.USER_UPDATE_SUCCESS))
                .andExpect(jsonPath("$.data.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.data.lastName").value("UpdatedLastName"));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFirstname()).isEqualTo("UpdatedFirstName");
        assertThat(updatedUser.getLastname()).isEqualTo("UpdatedLastName");
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully() throws Exception {
        // given
        User user = createAndSaveUser("changepassword@example.com");
        String accessToken = generateToken(user);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .userId(user.getId())
                .oldPassword("password")
                .newPassword("newPassword123!")
                .confirmPassword("newPassword123!")
                .build();

        // when & then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(AppConstants.PASSWORD_CHANGE_SUCCESS));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword123!", updatedUser.getPassword())).isTrue();
    }
}
