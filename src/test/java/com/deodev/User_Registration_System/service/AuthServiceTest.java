package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    private User user;

    private CustomUserDetails userDetails;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().name("ADMIN").build();
        userRole = Role.builder().name("USER").build();
        user = User.builder()
                .roles(Set.of(adminRole, userRole))
                .email("@email.com")
                .status(UserStatus.ACTIVE)
                .password("password")
                .build();
    }

    @Test
    void getAuthoritiesFromRoles_shouldReturnCorrectAuthorities() {
        List<String> authorities = authService.getAuthoritiesFromRoles(user);

        assertThat(authorities).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void getAuthorities_shouldReturnCorrectAuthorities() {
        // given
        userDetails = new CustomUserDetails(user);

        List<String> authorities = authService.getAuthorities(userDetails);

        assertThat(authorities).containsExactlyInAnyOrder("ADMIN", "USER");
    }
}
