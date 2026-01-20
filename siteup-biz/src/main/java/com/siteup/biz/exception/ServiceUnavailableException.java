package com.siteup.biz.exception;

/**
 * Exception thrown when external services are unavailable
 */
public class ServiceUnavailableException extends BizException {

    public ServiceUnavailableException(String serviceName) {
        super("SERVICE_UNAVAILABLE", serviceName + " service is currently unavailable");
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super("SERVICE_UNAVAILABLE", serviceName + " service is currently unavailable", cause);
    }
}
