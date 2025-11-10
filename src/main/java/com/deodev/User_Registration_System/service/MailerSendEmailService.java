package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.dto.EmailContent;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailerSendEmailService implements EmailService{

    @Value("${email.from}")
    private String appEmail;

    @Value("${email.api-key}")
    private String providerToken;

    @Override
    public void sendMail(EmailContent emailContent) {
        Email email = new Email();

        email.setFrom("user-registration-system", appEmail);
        email.addRecipient(emailContent.recipientName(), emailContent.recipientAddress());
        email.setSubject(emailContent.subject());
        email.setHtml(emailContent.template());

        MailerSend ms = new MailerSend();
        ms.setToken(providerToken);

        try {
            log.info("Sending Email to {}...", emailContent.recipientAddress());
            MailerSendResponse response = ms.emails().send(email);
            log.info("Email Sent: [{}]", response.headers);
        } catch (Exception ex) {
            log.error("Unexpected error while sending email to {}", emailContent.recipientAddress(), ex);
        }
    }
}
