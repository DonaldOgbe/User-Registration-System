package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.dto.EmailContent;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.response.emails.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MailTrapEmailService implements EmailService{

    @Value("${email.from}")
    private String appDomain;

    @Value("${email.api-key}")
    private String providerToken;

    @Override
    public void sendMail(EmailContent emailContent) {
        final var config = new MailtrapConfig.Builder()
                .token(providerToken)
                .build();

        final var client = MailtrapClientFactory.createMailtrapClient(config);

        final var mail = MailtrapMail.builder()
                .from(new Address(appDomain))
                .to(List.of(new Address(emailContent.recipientAddress())))
                .subject(emailContent.subject())
                .html(emailContent.template())
                .build();

        try {
            log.info("Sending Email to {}...", mail.getTo());
            SendResponse response = client.sendingApi().emails().send(mail);
            log.info("Email Sent: {}", response.isSuccess());
        } catch (Exception ex) {
            log.error("Unexpected error while sending email to {}", mail.getTo(), ex);
        }
    }
}
