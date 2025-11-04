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
    void activateUser_whenTokenIsValid_activatesUserAndDeletesToken() {
        // Given
        when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(verificationToken));
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // When
        verificationService.activateUser(TEST_TOKEN);

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userService, times(1)).saveUser(user);
        verify(tokenRepository, times(1)).delete(verificationToken);
        verify(emailService, times(1)).sendWelcomeEmail(user.getEmail(), user.getFirstname());
    }

    @Test
    void activateUser_whenTokenIsInvalid_throwsVerificationTokenException() {
        // Given
        when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

        // When & Then
        VerificationTokenException exception = assertThrows(VerificationTokenException.class, () ->
                verificationService.activateUser(TEST_TOKEN)
        );
        assertEquals(AppConstants.INVALID_VERIFICATION_TOKEN, exception.getMessage());
        verify(userService, never()).saveUser(any(User.class));
        verify(tokenRepository, never()).delete(any(VerificationToken.class));
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void activateUser_whenTokenIsExpired_throwsVerificationTokenException() {
        // Given
        verificationToken.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(verificationToken));

        // When & Then
        VerificationTokenException exception = assertThrows(VerificationTokenException.class, () ->
                verificationService.activateUser(TEST_TOKEN)
        );
        assertEquals(AppConstants.VERIFICATION_TOKEN_EXPIRED, exception.getMessage());
        verify(userService, never()).saveUser(any(User.class));
        verify(tokenRepository, never()).delete(any(VerificationToken.class));
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void register_whenUserRegisters_createsTokenAndSendsVerificationEmail() {
        // Given
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        // When
        VerificationToken result = verificationService.register(user);

        // Then
        assertNotNull(result);
        assertEquals(TEST_TOKEN, result.getToken());
        verify(userService, times(1)).saveUser(user);
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(user.getEmail()), eq(user.getFirstname()), anyString());
    }
}
