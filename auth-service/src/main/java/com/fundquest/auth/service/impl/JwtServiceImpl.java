package com.fundquest.auth.service.impl;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_EMAIL, user.getEmail());
        claims.put(AppConstants.CLAIM_USER_ID, user.getId());
        claims.put(AppConstants.CLAIM_TYPE, AppConstants.TOKEN_TYPE_ACCESS);

        return createToken(claims, user.getEmail(), AppConstants.ACCESS_TOKEN_EXPIRY);
    }

    @Override
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_EMAIL, user.getEmail());
        claims.put(AppConstants.CLAIM_USER_ID, user.getId());
        claims.put(AppConstants.CLAIM_TYPE, AppConstants.TOKEN_TYPE_REFRESH);

        return createToken(claims, user.getEmail(), AppConstants.REFRESH_TOKEN_EXPIRY);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public String extractEmailFromToken(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_EMAIL, String.class));
    }

    @Override
    public String extractUserIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_USER_ID, String.class));
    }

    @Override
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_TYPE, String.class));
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}