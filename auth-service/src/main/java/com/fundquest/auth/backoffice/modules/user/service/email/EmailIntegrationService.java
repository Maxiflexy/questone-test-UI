package com.fundquest.auth.backoffice.modules.user.service.email;

import com.fundquest.auth.client.EmailServiceClient;
import com.fundquest.auth.exception.BusinessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service for integrating with email service
 * Handles email sending and error scenarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailIntegrationService {

    private final EmailServiceClient emailServiceClient;

    private static final String BACKOFFICE_WELCOME_EMAIL = "backoffice-welcome-email";
    private static final HttpStatus EXPECTED_SUCCESS_STATUS = HttpStatus.ACCEPTED;

    /**
     * Send welcome email to invited user
     *
     * @param email Email address of the invited user
     * @throws BusinessException if email sending fails or returns unexpected status
     */
    public void sendWelcomeEmail(String email) {
        try {
            ResponseEntity<Void> response = emailServiceClient.sendEmail(email, BACKOFFICE_WELCOME_EMAIL);

            // Check if response status is the expected 202 ACCEPTED
            if (!EXPECTED_SUCCESS_STATUS.equals(response.getStatusCode())) {
                log.error("Email service returned unexpected status: {} for email: {}. Expected: {}",
                        response.getStatusCode(), email, EXPECTED_SUCCESS_STATUS);

                throw new BusinessException(
                        "Unable to invite user.",
                        "EMAIL_SERVICE_ERROR"
                );
            }

            log.info("Successfully sent welcome email to: {}", email);

        } catch (FeignException.FeignClientException e) {
            log.error("Feign client error when calling email service for email: {}. Status: {}, Message: {}",
                    email, e.status(), e.getMessage());

            throw new BusinessException(
                    "Unable to invite user. Email service error: " + e.getMessage(),
                    "EMAIL_SERVICE_CLIENT_ERROR"
            );

        } catch (FeignException.FeignServerException e) {
            log.error("Email service server error for email: {}. Status: {}, Message: {}",
                    email, e.status(), e.getMessage());

            throw new BusinessException(
                    "Unable to invite user. Email service is temporarily unavailable",
                    "EMAIL_SERVICE_SERVER_ERROR"
            );

        } catch (FeignException e) {
            log.error("Unexpected Feign exception when calling email service for email: {}. Message: {}",
                    email, e.getMessage());

            throw new BusinessException(
                    "Unable to invite user. Communication with email service failed",
                    "EMAIL_SERVICE_COMMUNICATION_ERROR"
            );

        } catch (Exception e) {
            log.error("Unexpected error when sending welcome email to: {}. Error: {}",
                    email, e.getMessage(), e);

            throw new BusinessException(
                    "Unable to invite user. Unexpected error occurred while sending email",
                    "EMAIL_PROCESSING_ERROR"
            );
        }
    }

    /**
     * Send welcome email with custom message type
     *
     * @param email Email address of the recipient
     * @param messageType Custom message type
     * @throws BusinessException if email sending fails
     */
    public void sendEmail(String email, String messageType) {
        log.info("Sending email with messageType: {} to: {}", messageType, email);

        try {
            ResponseEntity<Void> response = emailServiceClient.sendEmail(email, messageType);

            log.debug("Email service response status: {} for email: {} with messageType: {}",
                    response.getStatusCode(), email, messageType);

            if (!EXPECTED_SUCCESS_STATUS.equals(response.getStatusCode())) {
                log.error("Email service returned unexpected status: {} for email: {} with messageType: {}. Expected: {}",
                        response.getStatusCode(), email, messageType, EXPECTED_SUCCESS_STATUS);

                throw new BusinessException(
                        "Email service returned unexpected status: " + response.getStatusCode(),
                        "EMAIL_SERVICE_ERROR"
                );
            }

            log.info("Successfully sent email with messageType: {} to: {}", messageType, email);

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e; // Re-throw business exceptions
            }

            log.error("Error sending email with messageType: {} to: {}. Error: {}",
                    messageType, email, e.getMessage(), e);

            throw new BusinessException(
                    "Failed to send email: " + e.getMessage(),
                    "EMAIL_PROCESSING_ERROR"
            );
        }
    }
}
