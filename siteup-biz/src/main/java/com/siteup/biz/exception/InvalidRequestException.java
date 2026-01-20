package com.siteup.biz.exception;

/**
 * Exception thrown when request parameters are invalid
 */
public class InvalidRequestException extends BizException {

    public InvalidRequestException(String message) {
        super("INVALID_REQUEST", message);
    }
}
