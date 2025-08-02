package com.fundquest.email_service.exception;

import com.fundquest.email_service.dto.EmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<EmailResponse> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
        );

        logger.warn("Validation error: {}", errorMsg);
        return ResponseEntity.badRequest()
                .body(new EmailResponse(false, errorMsg.toString()));
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<EmailResponse> handleBindException(BindException ex) {
        StringBuilder errorMsg = new StringBuilder("Binding error: ");
        ex.getFieldErrors().forEach(error ->
                errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
        );

        logger.warn("Binding error: {}", errorMsg);
        return ResponseEntity.badRequest()
                .body(new EmailResponse(false, errorMsg.toString()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<EmailResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EmailResponse(false, "An unexpected error occurred: " + ex.getMessage()));
    }
}