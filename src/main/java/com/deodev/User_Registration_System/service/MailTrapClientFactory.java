package com.deodev.User_Registration_System.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.stereotype.Component;

@Component
public class MailTrapClientFactory {
    public MailtrapClient createMailTrapClient(String token) {
        final var config = new MailtrapConfig.Builder()
                .token(token)
                .build();

        return MailtrapClientFactory.createMailtrapClient(config);
    }
}
