package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendWelcomeEmail(User user){
        System.out.printf("Welcome %s !! We are Happy to have you.%n", user.getName());
    }
}
