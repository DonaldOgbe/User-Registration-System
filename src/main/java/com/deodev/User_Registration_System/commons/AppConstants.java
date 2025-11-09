package com.deodev.User_Registration_System.commons;

public class AppConstants {

    // Success
    public static final String USER_REGISTRATION_SUCCESS = "User registered successfully. Please check your email to verify your account.";
    public static final String USER_VERIFICATION_SUCCESS = "Account verified successfully.";
    public static final String USER_LOGIN_SUCCESS = "Login successful";
    public static final String REFRESH_TOKEN_SUCCESS = "Token refreshed successfully";
    public static final String USER_FETCH_SUCCESS = "User fetched successfully.";
    public static final String USER_UPDATE_SUCCESS = "User updated successfully.";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully.";

    // Error
    public static final String USER_REGISTRATION_FAILURE = "User not registered";
    public static final String INVALID_VERIFICATION_TOKEN = "Invalid verification token.";
    public static final String VERIFICATION_TOKEN_EXPIRED = "Verification token has expired.";
    public static final String VERIFICATION_FAILED = "Account verification failed.";
    public static final String USER_LOGIN_FAILURE = "User login failed";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String NOT_FOUND = "Not found";
    public static final String INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String REFRESH_TOKEN_INVALID = "Invalid refresh token.";
    public static final String ACCESS_TOKEN_INVALID = "Invalid or expired token.";
    public static final String ROLE_NOT_FOUND = "Role not found.";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists.";
    public static final String INVALID_OLD_PASSWORD = "Old password is not correct.";
    public static final String PASSWORD_MISMATCH = "New password and confirm password do not match.";
    public static final String FAILED_AUTHORIZATION = "Authorization failed.";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error.";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters long.";
    public static final String PASSWORD_MUST_CONTAIN_UPPERCASE = "Password must contain at least one uppercase letter.";
    public static final String PASSWORD_MUST_CONTAIN_LOWERCASE = "Password must contain at least one lowercase letter.";
    public static final String PASSWORD_MUST_CONTAIN_DIGIT = "Password must contain at least one digit.";
    public static final String PASSWORD_MUST_CONTAIN_SPECIAL_CHAR = "Password must contain at least one special character.";


    private AppConstants() {
    }
}