package com.siteup.auth.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends AuthException {

    public UserNotFoundException(String username) {
        super("USER_NOT_FOUND", "User not found: " + username);
    }
}
