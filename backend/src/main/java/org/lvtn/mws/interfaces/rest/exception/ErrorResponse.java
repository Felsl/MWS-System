package org.lvtn.mws.interfaces.rest.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private int status;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    public ErrorResponse() { this.timestamp = LocalDateTime.now(); }

    public ErrorResponse(int status, String errorCode, String message) {
        this();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public List<FieldError> getErrors() { return errors; }
    public void setErrors(List<FieldError> errors) { this.errors = errors; }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
