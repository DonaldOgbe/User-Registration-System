package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.model.User;
import jdk.jfr.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalStream = System.out;

    EmailService testEmailService;

    @BeforeEach
    void setup(){
        System.setOut(new PrintStream(outputStream));
        testEmailService = new EmailService();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalStream);
    }

    @Test @Description("print welcome message to console")
    void getUserNameAndPrintToConsole() {
        User testUser = new User("User", "user@gmail.com");
        testEmailService.sendWelcomeEmail(testUser);
        String message = String.format("Welcome %s !! We are Happy to have you.%n", testUser.getName());
        assertEquals(outputStream.toString().trim(), (message.trim()));
    }

}