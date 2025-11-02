package com.deodev.User_Registration_System.controller;

import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    public String register(@RequestBody User user) {
       return userService.registerUser(user);
    }



}
