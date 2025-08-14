package com.fundquest.auth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for EmailServiceClient
 * Provides graceful degradation when email service is unavailable
 */
@Component
@Slf4j
public class EmailServiceClientFallback implements EmailServiceClient {

    @Override
    public ResponseEntity<Void> sendEmail(String email, String messageType) {
        log.error("Email service is unavailable. Fallback triggered for email: {} with messageType: {}",
                email, messageType);

        // Return a response that will trigger business exception
        // This ensures transaction rollback when email service is down
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}