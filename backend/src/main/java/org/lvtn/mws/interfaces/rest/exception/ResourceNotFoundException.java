package org.lvtn.mws.interfaces.rest.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
