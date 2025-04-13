package com.deodev.User_Registration_System.repository;

import com.deodev.User_Registration_System.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {
    private final Map<Long, User> database = new HashMap<>();

    public boolean containsUser(User user) {
        return database.containsKey(user.getId());
    }

    public void addUser(User user) {
        database.put(user.getId(), user);
    }

    public User findUserById(long id) {
        return database.get(id);
    }

    public ArrayList<User> findAllUsers() {
        return new ArrayList<>(database.values());
    }
}
