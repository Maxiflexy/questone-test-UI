package com.example.auth_demo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.auth_demo.model.User;
import com.example.auth_demo.repository.UserRepository;
import com.example.auth_demo.security.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebClient webClient;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.redirect-uri}")
    private String redirectUri;

    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeCodeForToken(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Authorization code is required");
        }

        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);

        // Add client_secret for Web apps (required for confidential clients)
        if (clientSecret != null && !clientSecret.isEmpty()) {
            params.add("client_secret", clientSecret);
        }

        try {
            Mono<ResponseEntity<String>> responseMono = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .toEntity(String.class);

            ResponseEntity<String> response = responseMono.block();

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String idToken = jsonResponse.getString("id_token");

                try {

                    DecodedJWT decodedJWT = JWT.decode(idToken);

                    // Extract user information from the decoded JWT
                    String userId = decodedJWT.getClaim("oid").asString();
                    String name = decodedJWT.getClaim("name").asString();
                    String email = decodedJWT.getClaim("email").asString();
                    String preferredUsername = decodedJWT.getClaim("preferred_username").asString();

                    // Handle cases where claims might be null
                    if (userId == null || userId.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Invalid ID token: missing user ID");
                    }

                    // Use fallback values if name or email are null
                    name = (name != null && !name.isEmpty()) ? name : "Unknown User";
                    email = (email != null && !email.isEmpty()) ? email :
                            (preferredUsername != null && !preferredUsername.isEmpty()) ? preferredUsername : "unknown@example.com";

                    System.out.println("Expires At: " + decodedJWT.getExpiresAt());

                    // Save or update user in database
                    User user = userRepository.findById(userId).orElse(new User());
                    user.setId(userId);
                    user.setName(name);
                    user.setEmail(email);
                    userRepository.save(user);

                    // Generate JWT token for the application
                    String jwtToken = jwtUtil.generateToken(user);
                    return ResponseEntity.ok(jwtToken);

                } catch (Exception jwtException) {
                    System.err.println("Error decoding ID token: " + jwtException.getMessage());
                    jwtException.printStackTrace();
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Invalid ID token: " + jwtException.getMessage());
                }

            } else {
                String errorBody = response != null ? response.getBody() : "Unknown error";
                System.err.println("Token exchange failed: " + errorBody);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token exchange failed: " + errorBody);
            }

        } catch (WebClientResponseException e) {
            System.err.println("=== Microsoft Token Exchange Error ===");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            System.err.println("Headers: " + e.getHeaders());

            String errorDetails = "Microsoft token exchange failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);

        } catch (Exception e) {
            System.err.println("Error during token exchange: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during token exchange: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint to verify the auth service is running
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    /**
     * Endpoint to get current user information from JWT token
     * Now uncommented and updated to work with the new JwtUtil
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid authorization header");
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // Validate token first
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired token");
            }

            // Extract user ID from the validated token
            String userId = jwtUtil.extractUserId(token);

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token: no user ID found");
            }

            // Get user from database
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user information");
        }
    }
}