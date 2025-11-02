package com.deodev.User_Registration_System.commons;

public class AppConstants {

    public static final String USER_REGISTRATION_SUCCESS = "User registered successfully. Please check your email to verify your account.";
    public static final String USER_VERIFICATION_SUCCESS = "Account verified successfully.";
    public static final String INVALID_VERIFICATION_TOKEN = "Invalid verification token.";
    public static final String VERIFICATION_TOKEN_EXPIRED = "Verification token has expired.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String REFRESH_TOKEN_INVALID = "Invalid refresh token.";
    public static final String ROLE_NOT_FOUND = "Role not found.";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists.";

    private AppConstants() {
        // restrict instantiation
    }
}
