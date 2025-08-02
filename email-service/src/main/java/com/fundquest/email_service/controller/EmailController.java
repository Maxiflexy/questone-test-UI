package com.fundquest.email_service.controller;

import com.fundquest.email_service.dto.EmailRequest;
import com.fundquest.email_service.dto.EmailResponse;
import com.fundquest.email_service.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;

    @Qualifier("emailTaskExecutor")
    private final TaskExecutor taskExecutor;

    @Autowired
    public EmailController(EmailService emailService, @Qualifier("emailTaskExecutor") TaskExecutor taskExecutor) {
        this.emailService = emailService;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/send-welcome")
    public ResponseEntity<EmailResponse> sendWelcomeEmail(@Valid @RequestBody EmailRequest emailRequest,
                                                          BindingResult bindingResult) {

        logger.info("Received request to send welcome email to: {}", emailRequest.getEmail());

        // Validate request
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder("Validation errors: ");
            bindingResult.getFieldErrors().forEach(error ->
                    errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
            );

            logger.warn("Validation failed for email request: {}", errorMsg);
            return ResponseEntity.badRequest()
                    .body(new EmailResponse(false, errorMsg.toString()));
        }

        try {
            taskExecutor.execute(() -> {
                try {
                    logger.info("Starting background email send task for: {}", emailRequest.getEmail());
                    emailService.sendWelcomeEmail(emailRequest.getEmail());
                } catch (Exception e) {
                    logger.error("Background email task threw exception for: {}. Error: {}",
                            emailRequest.getEmail(), e.getMessage(), e);
                }
            });

            logger.info("Email queued for background processing: {}", emailRequest.getEmail());
            return ResponseEntity.accepted()
                    .body(new EmailResponse(true, "Email sent successfully"));

        } catch (Exception e) {
            logger.error("Unexpected error while sending email to: {}. Error: {}",
                    emailRequest.getEmail(), e.getMessage(), e);

            EmailResponse errorResponse = new EmailResponse(false,
                    "An unexpected error occurred while sending the email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok("Email service is running successfully!");
    }
}