package com.fundquest.auth.service;

import com.fundquest.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String USER_ID_CLAIM = "user_id";
    private static final String EMAIL_CLAIM = "email";
    private static final String NAME_CLAIM = "name";
    private static final String MICROSOFT_ID_CLAIM = "microsoft_id";
    private static final String AUTHORITIES_CLAIM = "authorities";

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, user.getId().toString());
        claims.put(EMAIL_CLAIM, user.getEmail());
        claims.put(NAME_CLAIM, user.getName());
        claims.put(MICROSOFT_ID_CLAIM, user.getMicrosoftId());
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        claims.put(AUTHORITIES_CLAIM, "USER"); // Default role for now

        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Generate access token from authentication
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        claims.put(AUTHORITIES_CLAIM, authorities);

        return createToken(claims, username, accessTokenExpiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, user.getId().toString());
        claims.put(EMAIL_CLAIM, user.getEmail());
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Get user ID from JWT token
     */
    public UUID getUserIdFromToken(String token) {
        String userIdStr = getClaimFromToken(token, claims -> claims.get(USER_ID_CLAIM, String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    /**
     * Get email from JWT token
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(EMAIL_CLAIM, String.class));
    }

    /**
     * Get token type from JWT token
     */
    public String getTokenTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Get authorities from JWT token
     */
    public String getAuthoritiesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(AUTHORITIES_CLAIM, String.class));
    }

    /**
     * Get expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Get specific claim from JWT token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from JWT token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if JWT token is expired
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Validate JWT token structure and signature
     */
    public Boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if token is access token
     */
    public Boolean isAccessToken(String token) {
        String tokenType = getTokenTypeFromToken(token);
        return ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    /**
     * Check if token is refresh token
     */
    public Boolean isRefreshToken(String token) {
        String tokenType = getTokenTypeFromToken(token);
        return REFRESH_TOKEN_TYPE.equals(tokenType);
    }

    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    /**
     * Parse JWT token and get user details
     */
    public Map<String, Object> parseUserFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Map<String, Object> userDetails = new HashMap<>();

        userDetails.put("userId", claims.get(USER_ID_CLAIM));
        userDetails.put("email", claims.get(EMAIL_CLAIM));
        userDetails.put("name", claims.get(NAME_CLAIM));
        userDetails.put("microsoftId", claims.get(MICROSOFT_ID_CLAIM));
        userDetails.put("authorities", claims.get(AUTHORITIES_CLAIM));
        userDetails.put("tokenType", claims.get(TOKEN_TYPE_CLAIM));
        userDetails.put("subject", claims.getSubject());
        userDetails.put("issuedAt", claims.getIssuedAt());
        userDetails.put("expiresAt", claims.getExpiration());

        return userDetails;
    }
}