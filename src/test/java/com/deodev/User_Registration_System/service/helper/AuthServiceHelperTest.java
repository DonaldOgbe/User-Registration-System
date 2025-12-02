package com.deodev.User_Registration_System.service.helper;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceHelperTest {

    @InjectMocks
    private AuthServiceHelper authServiceHelper;

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
        List<String> authorities = authServiceHelper.getAuthoritiesFromRoles(user);

        assertThat(authorities).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void getAuthorities_shouldReturnCorrectAuthorities() {
        // given
        userDetails = new CustomUserDetails(user);

        List<String> authorities = authServiceHelper.getAuthorities(userDetails);

        assertThat(authorities).containsExactlyInAnyOrder("ADMIN", "USER");
    }
}