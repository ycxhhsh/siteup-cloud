package com.siteup.biz.exception;

/**
 * Base exception for business logic errors
 */
public abstract class BizException extends RuntimeException {

    private final String errorCode;

    protected BizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BizException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
