package com.fundquest.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service to handle security context operations
 * Provides reusable methods for extracting authentication information
 */
@Service
@Slf4j
public class SecurityContextService {

    /**
     * Get the authenticated user's email from security context
     * @return authenticated user's email
     * @throws IllegalStateException if no authentication found or email is null
     */
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("No authentication found in security context");
            throw new IllegalStateException("No authentication found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String email)) {
            log.error("Expected String email but got: {}", principal != null ? principal.getClass() : "null");
            throw new IllegalStateException("Invalid principal type in authentication");
        }

        if (email.trim().isEmpty()) {
            log.error("Email is null or empty in authentication principal");
            throw new IllegalStateException("Email not found in authentication");
        }
        return email;
    }

    /**
     * Get the current authentication object
     * @return current Authentication or null if not authenticated
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Check if current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Get authenticated user's email safely (returns null if not authenticated)
     * @return authenticated user's email or null
     */
    public String getAuthenticatedUserEmailSafely() {
        try {
            return getAuthenticatedUserEmail();
        } catch (IllegalStateException e) {
            log.debug("Failed to get authenticated user email safely: {}", e.getMessage());
            return null;
        }
    }
}
