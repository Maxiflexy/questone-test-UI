package com.fundquest.auth.constants;

public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // API Endpoints
    public static final String AUTH_BASE_PATH = "/api/v1/auth";
    public static final String MICROSOFT_VERIFY_ENDPOINT = "/microsoft/verify";
    public static final String REFRESH_TOKEN_ENDPOINT = "/refresh";
    public static final String LOGOUT_ENDPOINT = "/logout";
    public static final String USER_PROFILE_ENDPOINT = "/api/v1/auth/user/profile";

    // JWT Constants
    public static final String JWT_HEADER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000L; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRY = 60 * 60 * 1000L; // 1 hour

    // Cookie Constants
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final int COOKIE_MAX_AGE = 3600; // 1 hour in seconds

    // Microsoft OAuth Constants
    public static final String MICROSOFT_TOKEN_URL = "https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token";
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";

    // Error Codes
    public static final String INVALID_AUTH_CODE = "INVALID_AUTH_CODE";
    public static final String AUTH_CODE_EXPIRED = "AUTH_CODE_EXPIRED";
    public static final String INVALID_TENANT = "INVALID_TENANT";
    public static final String NO_REFRESH_TOKEN = "NO_REFRESH_TOKEN";//
    public static final String INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";

    // JWT Claims
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_TYPE = "type";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    // Success Messages
    public static final String LOGOUT_SUCCESS = "Successfully logged out";

    // Token Type
    public static final String BEARER_TOKEN_TYPE = "Bearer";
}