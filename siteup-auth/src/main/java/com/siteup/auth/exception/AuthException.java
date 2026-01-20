package com.siteup.auth.exception;

/**
 * Base exception for authentication and authorization errors
 */
public abstract class AuthException extends RuntimeException {

    private final String errorCode;

    protected AuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
