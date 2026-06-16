package org.lvtn.mws.interfaces.rest.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}
