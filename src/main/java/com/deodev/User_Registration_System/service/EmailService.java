package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.dto.EmailContent;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async("taskExecutor")
    void sendMail(EmailContent emailContent);

    @Async("taskExecutor")
    void sendVerificationEmail(String to, String userName, String activationLink);

    @Async("taskExecutor")
    void sendWelcomeEmail(String to, String userName);
}
