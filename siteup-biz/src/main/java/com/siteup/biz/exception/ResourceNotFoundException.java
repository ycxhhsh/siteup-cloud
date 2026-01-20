package com.siteup.biz.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BizException {

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super("RESOURCE_NOT_FOUND", resourceType + " not found: " + resourceId);
    }
}
