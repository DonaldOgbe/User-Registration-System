package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.repository.UserRepository;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    UserService testUserService = new UserService(new UserRepository(), new EmailService());

    @Test @Description("add user to database, print welcome message and return confirmation message")
    void returnConfirmationMessage() {
        String message = testUserService.registerUser(new User("User", "user@gmail.com"));
        assertEquals("User has been registered !", message);
    }
}