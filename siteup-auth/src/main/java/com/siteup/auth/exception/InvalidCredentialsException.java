package com.siteup.auth.exception;

/**
 * Exception thrown when login credentials are invalid
 */
public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid username or password");
    }
}
