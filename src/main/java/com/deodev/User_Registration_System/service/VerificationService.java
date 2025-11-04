package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.exception.VerificationTokenException;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.model.VerificationToken;
import com.deodev.User_Registration_System.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public VerificationToken createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .build();
        return tokenRepository.save(verificationToken);
    }

    @Transactional
    public void activateUser(String token) {
        log.info("Attempting to activate user with token: {}", token);
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid verification token: {}", token);
                    return new VerificationTokenException(AppConstants.INVALID_VERIFICATION_TOKEN);
                });

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification token expired for user: {}", verificationToken.getUser().getEmail());
            throw new VerificationTokenException(AppConstants.VERIFICATION_TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userService.saveUser(user);
        tokenRepository.delete(verificationToken);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstname());
        log.info("User {} activated successfully and welcome email sent.", user.getEmail());
    }
}
