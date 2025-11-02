package com.deodev.User_Registration_System.controller;

import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.repository.UserRepository;
import com.deodev.User_Registration_System.service.EmailService;
import com.deodev.User_Registration_System.service.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    UserController testUserController = new UserController(new UserService(new UserRepository(), new EmailService()));

    @Test
    void register() {
        String message = testUserController.register(new User("User", "user@gmail.com"));
        assertEquals("User has been registered !", message.trim());
    }
}