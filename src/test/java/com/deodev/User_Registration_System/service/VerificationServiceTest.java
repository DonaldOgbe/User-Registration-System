package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.exception.VerificationTokenException;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.VerificationToken;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private TemplateService templateService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    private User user;
    private VerificationToken verificationToken;
    private final String TEST_TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .status(UserStatus.PENDING)
                .build();

        verificationToken = VerificationToken.builder()
                .id(UUID.randomUUID())
                .token(TEST_TOKEN)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        ReflectionTestUtils.setField(verificationService, "appBaseUrl", "http://localhost:8080");
    }

    @Test
    void activateUser_ShouldActivateUserAndDeleteToken_WhenTokenIsValid() {
        // given
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));
        when(templateService.buildWelcomeEmailContent(any())).thenReturn(any());

        // when
        verificationService.activateUser("valid-token");

        // then
        verify(tokenRepository).findByToken("valid-token");
        verify(userService).saveUser(user);
        verify(tokenRepository).delete(verificationToken);

        // user should now be ACTIVE
        assert user.getStatus() == UserStatus.ACTIVE;
    }

    @Test
    void activateUser_ShouldThrowException_WhenTokenNotFound() {
        // given
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // when / then
        try {
            verificationService.activateUser("invalid-token");
        } catch (VerificationTokenException ex) {
            assert ex.getMessage().equals(AppConstants.INVALID_VERIFICATION_TOKEN);
        }

        verify(tokenRepository).findByToken("invalid-token");
        verifyNoMoreInteractions(tokenRepository, userService);
    }

    @Test
    void activateUser_ShouldThrowException_WhenTokenExpired() {
        // given
        verificationToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // expired
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(verificationToken));

        // when / then
        try {
            verificationService.activateUser("expired-token");
        } catch (VerificationTokenException ex) {
            assert ex.getMessage().equals(AppConstants.VERIFICATION_TOKEN_EXPIRED);
        }

        verify(tokenRepository).findByToken("expired-token");
        verify(userService, never()).saveUser(any());
        verify(tokenRepository, never()).delete(any());
    }
}
