package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository database;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository database, EmailService emailService) {
        this.database = database;
        this.emailService = emailService;
    }

    public String registerUser(User user) {
        database.addUser(user);
        emailService.sendWelcomeEmail(user);
        return String.format("%s has been registered !", user.getName());
    }

}
