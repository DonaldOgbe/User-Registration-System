package com.deodev.User_Registration_System.model;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    User testUser;

    @BeforeEach
    void setup() {
        testUser = new User("User", "user@gmail.com");
    }

    @Test @Description("return user name")
    void getName() {
        assertEquals("User", testUser.getName());
    }

    @Test @Description("return user email")
    void getEmail() {
        assertEquals("user@gmail.com", testUser.getEmail());
    }

    @Test @Description("return user id")
    void getId() {
        assertEquals(1, testUser.getId());
    }
}