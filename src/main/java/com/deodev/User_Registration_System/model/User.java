package com.deodev.User_Registration_System.model;

import java.util.Objects;


public class User {
    private String name;
    private String email;
    private long id;
    private static long idCount = 1;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        id = hashCode();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return name.equals(user.name) && email.equals(user.email) && id == ((User) obj).getId();
    }


}
