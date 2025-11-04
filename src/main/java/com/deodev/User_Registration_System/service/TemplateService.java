package com.deodev.User_Registration_System.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateEngine templateEngine;

    public String buildEmail(String template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(template, context);
    }

    public String buildVerificationEmailContent(String userName, String activationLink) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "activationLink", activationLink
        );
        return buildEmail("verification-email", variables);
    }

    public String buildWelcomeEmailContent(String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName
        );
        return buildEmail("welcome-email", variables);
    }
}
