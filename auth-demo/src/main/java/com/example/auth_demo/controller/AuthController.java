package com.example.auth_demo.controller;

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
import reactor.core.publisher.Mono;

import java.util.Base64;

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
    public ResponseEntity<String> exchangeCodeForToken(@RequestParam String code) {
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        try {
            Mono<ResponseEntity<String>> responseMono = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .toEntity(String.class);

            ResponseEntity<String> response = responseMono.block();
            System.out.println(response);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String idToken = jsonResponse.getString("id_token");

                // Decode ID token manually (for simplicity; in production, use a JWT library)
                String[] chunks = idToken.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                JSONObject payloadJson = new JSONObject(payload);

                String userId = payloadJson.getString("oid");
                String name = payloadJson.optString("name", "Unknown");
                String email = payloadJson.optString("email", "Unknown");

                // Save or update user
                User user = userRepository.findById(userId).orElse(new User());
                user.setId(userId);
                user.setName(name);
                user.setEmail(email);
                userRepository.save(user);

                // Generate JWT token
                String jwtToken = jwtUtil.generateToken(user);
                return ResponseEntity.ok(jwtToken);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token exchange failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during token exchange: " + e.getMessage());
        }
    }
}