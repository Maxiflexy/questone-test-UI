package com.fundquest.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client interface for Email Service
 */
@FeignClient(
        name = "EMAIL-SERVICE", // Service name registered in Eureka
        fallback = EmailServiceClientFallback.class // Fallback implementation for resilience
)
public interface EmailServiceClient {

    /**
     * Send email notification via email service
     *
     * @param email Email address to send notification to
     * @param messageType Type of message to send (e.g., "backoffice-welcome-email")
     * @return ResponseEntity with HTTP status from email service
     */
    @PostMapping("/api/email/send")
    ResponseEntity<Void> sendEmail(
            @RequestParam("email") String email,
            @RequestParam("messageType") String messageType
    );
}