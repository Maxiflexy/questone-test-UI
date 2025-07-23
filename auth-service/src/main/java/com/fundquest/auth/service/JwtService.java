package com.fundquest.auth.service;

import com.fundquest.auth.entity.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    String extractEmailFromToken(String token);
    String extractUserIdFromToken(String token);
    boolean isTokenExpired(String token);
    String extractTokenType(String token);
}
