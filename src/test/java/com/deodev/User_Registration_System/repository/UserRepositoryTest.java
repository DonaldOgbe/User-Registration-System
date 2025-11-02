package com.deodev.User_Registration_System.repository;

import com.deodev.User_Registration_System.model.User;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    UserRepository testRepository;
    User testUser;

    @BeforeEach
    void setup() {
        testRepository = new UserRepository();
        testUser = new User("User", "user@gmail.com");
    }

    @Test @Description("check if database contains user using user id")
    void containsUser() {
        assertTrue(testRepository.containsUser(testUser));
    }





}
