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

    @Test @Description("add user to the database")
    void addUser() {
        testRepository.addUser(testUser);
        assertTrue(testRepository.containsUser(testUser));
    }

    @Test @Description("get and return user by id")
    void getUser() {
        testRepository.addUser(testUser);
        User result = testRepository.findUserById(1);
        assertEquals(1, result.getId());
    }

    @Test @Description("get all users from database and return as an array of user objects")
    void getAllUsers() {
        testRepository.addUser(new User("User1", "user1@gmail.com"));
        testRepository.addUser(new User("User2", "user2@gmail.com"));

        ArrayList<User> result = testRepository.findAllUsers();

        ArrayList<User> expected = new ArrayList<>(Arrays.asList(new User("User1", "user1@gmail.com"),
                new User("User2", "user2@gmail.com")));

        assertEquals(expected, result);
    }
}
