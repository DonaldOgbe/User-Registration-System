package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.commons.AppConstants;
import com.deodev.User_Registration_System.exception.VerificationTokenException;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.model.enums.UserStatus;
import com.deodev.User_Registration_System.model.VerificationToken;
import com.deodev.User_Registration_System.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public VerificationToken register(User user) {
        User savedUser = userService.saveUser(user);
        VerificationToken verificationToken = createVerificationToken(savedUser);
        String activationLink = appBaseUrl + "/api/v1/auth/verify?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstname(), activationLink);
        return verificationToken;
    }

    @Transactional
    public void activateUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenException(AppConstants.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationTokenException(AppConstants.VERIFICATION_TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userService.saveUser(user);
        tokenRepository.delete(verificationToken);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstname());
    }
}
