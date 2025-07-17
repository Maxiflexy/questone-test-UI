package com.fundquest.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundquest.auth.exception.TokenValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class MicrosoftTokenService {

    @Value("${microsoft.oauth2.client-id}")
    private String clientId;

    @Value("${microsoft.oauth2.tenant-id}")
    private String tenantId;

    @Value("${microsoft.oauth2.jwks-uri}")
    private String jwksUri;

    @Value("${microsoft.oauth2.authority}")
    private String authority;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, PublicKey> publicKeyCache = new HashMap<>();

    public MicrosoftTokenService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Verify Microsoft ID token and extract user information
     */
    public Map<String, Object> verifyIdToken(String idToken) {
        try {
            // Parse token header to get key ID
            String[] tokenParts = idToken.split("\\.");
            System.out.println(Arrays.toString(tokenParts));
            if (tokenParts.length != 3) {
                throw new TokenValidationException("Invalid token format");
            }

            // Decode header
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            JsonNode header = objectMapper.readTree(headerJson);
            String keyId = header.get("kid").asText();

            // Get public key for verification
            PublicKey publicKey = getPublicKey(keyId);

            // Verify token signature and get claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(idToken)
                    .getBody();

            // Validate token claims
            validateTokenClaims(claims);

            // Extract user information
            return extractUserInfo(claims);

        } catch (Exception e) {
            throw new TokenValidationException("Failed to verify Microsoft ID token: " + e.getMessage());
        }
    }

    /**
     * Get public key from Microsoft JWKS endpoint
     */
    private PublicKey getPublicKey(String keyId) {
        // Check cache first
        if (publicKeyCache.containsKey(keyId)) {
            return publicKeyCache.get(keyId);
        }

        try {
            // Fetch JWKS from Microsoft
            String jwksResponse = webClient.get()
                    .uri(jwksUri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jwks = objectMapper.readTree(jwksResponse);
            JsonNode keys = jwks.get("keys");

            // Find the key with matching kid
            for (JsonNode key : keys) {
                if (keyId.equals(key.get("kid").asText())) {
                    PublicKey publicKey = createPublicKey(key);
                    publicKeyCache.put(keyId, publicKey);
                    return publicKey;
                }
            }

            throw new TokenValidationException("Public key not found for key ID: " + keyId);

        } catch (Exception e) {
            throw new TokenValidationException("Failed to get public key: " + e.getMessage());
        }
    }

    /**
     * Create RSA public key from JWK
     */
    private PublicKey createPublicKey(JsonNode key) throws Exception {
        String nStr = key.get("n").asText();
        String eStr = key.get("e").asText();

        byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
        byte[] eBytes = Base64.getUrlDecoder().decode(eStr);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * Validate Microsoft ID token claims
     */
    private void validateTokenClaims(Claims claims) {
        // Validate issuer
        String issuer = claims.getIssuer();
        String expectedIssuer = authority + "/v2.0";
        if (!expectedIssuer.equals(issuer)) {
            throw new TokenValidationException("Invalid issuer: " + issuer);
        }

        // Validate audience
        String audience = claims.getAudience();
        if (!clientId.equals(audience)) {
            throw new TokenValidationException("Invalid audience: " + audience);
        }

        // Validate tenant ID
        String tokenTenantId = (String) claims.get("tid");
        if (!tenantId.equals(tokenTenantId)) {
            throw new TokenValidationException("Invalid tenant ID: " + tokenTenantId);
        }

        // Validate expiration
        if (claims.getExpiration().before(java.util.Date.from(Instant.now()))) {
            throw new TokenValidationException("Token has expired");
        }

        // Validate token type (should be ID token)
        String tokenType = (String) claims.get("typ");
        if (tokenType != null && !"JWT".equals(tokenType)) {
            throw new TokenValidationException("Invalid token type: " + tokenType);
        }
    }

    /**
     * Extract user information from verified claims
     */
    private Map<String, Object> extractUserInfo(Claims claims) {
        Map<String, Object> userInfo = new HashMap<>();

        // Required fields
        userInfo.put("microsoftId", claims.get("oid")); // Object ID
        userInfo.put("email", claims.get("email", String.class));
        userInfo.put("name", claims.get("name", String.class));
        userInfo.put("tenantId", claims.get("tid", String.class));

        // Optional fields
        userInfo.put("givenName", claims.get("given_name", String.class));
        userInfo.put("familyName", claims.get("family_name", String.class));
        userInfo.put("preferredUsername", claims.get("preferred_username", String.class));
        userInfo.put("jobTitle", claims.get("jobTitle", String.class));
        userInfo.put("department", claims.get("department", String.class));
        userInfo.put("officeLocation", claims.get("officeLocation", String.class));
        userInfo.put("mobilePhone", claims.get("mobilePhone", String.class));
        userInfo.put("businessPhones", claims.get("businessPhones", String.class));

        // Additional token info
        userInfo.put("issuedAt", claims.getIssuedAt());
        userInfo.put("expiresAt", claims.getExpiration());
        userInfo.put("issuer", claims.getIssuer());
        userInfo.put("audience", claims.getAudience());

        return userInfo;
    }

    /**
     * Get user profile from Microsoft Graph API
     */
    public Map<String, Object> getUserProfile(String accessToken) {
        try {
            String graphApiUrl = "https://graph.microsoft.com/v1.0/me";

            String response = webClient.get()
                    .uri(graphApiUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode profile = objectMapper.readTree(response);
            Map<String, Object> userProfile = new HashMap<>();

            // Extract profile information
            userProfile.put("microsoftId", profile.get("id").asText());
            userProfile.put("email", profile.get("mail") != null ? profile.get("mail").asText() :
                    profile.get("userPrincipalName").asText());
            userProfile.put("name", profile.get("displayName").asText());
            userProfile.put("givenName", profile.get("givenName") != null ? profile.get("givenName").asText() : null);
            userProfile.put("familyName", profile.get("surname") != null ? profile.get("surname").asText() : null);
            userProfile.put("jobTitle", profile.get("jobTitle") != null ? profile.get("jobTitle").asText() : null);
            userProfile.put("department", profile.get("department") != null ? profile.get("department").asText() : null);
            userProfile.put("officeLocation", profile.get("officeLocation") != null ? profile.get("officeLocation").asText() : null);
            userProfile.put("mobilePhone", profile.get("mobilePhone") != null ? profile.get("mobilePhone").asText() : null);
            userProfile.put("preferredLanguage", profile.get("preferredLanguage") != null ? profile.get("preferredLanguage").asText() : null);

            return userProfile;

        } catch (Exception e) {
            throw new TokenValidationException("Failed to get user profile from Microsoft Graph: " + e.getMessage());
        }
    }

    /**
     * Validate Microsoft access token
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            getUserProfile(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear public key cache (useful for key rotation)
     */
    public void clearPublicKeyCache() {
        publicKeyCache.clear();
    }
}