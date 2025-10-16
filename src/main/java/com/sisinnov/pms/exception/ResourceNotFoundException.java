package com.sisinnov.pms.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, UUID id) {
        super(String.format("%s not found with id: %s", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: %s", resourceType, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}