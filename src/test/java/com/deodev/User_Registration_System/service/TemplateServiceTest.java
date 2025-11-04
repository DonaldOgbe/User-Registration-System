package com.deodev.User_Registration_System.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        templateService = new TemplateService(templateEngine);
    }

    @Test
    void buildVerificationEmailContent_whenCalledWithData_returnsCorrectHtml() {
        // Given
        String userName = "John Doe";
        String activationLink = "http://localhost:8080/verify?token=123";

        // When
        String htmlContent = templateService.buildVerificationEmailContent(userName, activationLink);

        // Then
        assertTrue(htmlContent.contains("Hello John Doe,"));
        assertTrue(htmlContent.contains("href=\"http://localhost:8080/verify?token=123\""));
    }

    @Test
    void buildWelcomeEmailContent_whenCalledWithData_returnsCorrectHtml() {
        // Given
        String userName = "John Doe";

        // When
        String htmlContent = templateService.buildWelcomeEmailContent(userName);

        // Then
        assertTrue(htmlContent.contains("Welcome John Doe,"));
    }
}