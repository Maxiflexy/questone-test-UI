package com.fundquest.auth.config.swagger;

/**
 * Constants for Swagger/OpenAPI documentation
 * Contains all reusable documentation strings for consistent API documentation
 */
public final class SwaggerConstants {

    private SwaggerConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // =========================== TAGS ===========================
    public static final String AUTH_TAG = "Authentication";
    public static final String USER_TAG = "User Management";
    public static final String PERMISSION_TAG = "Permission Management";
    public static final String ROLE_TAG = "Role Management";

    // =========================== TAG DESCRIPTIONS ===========================
    public static final String AUTH_TAG_DESCRIPTION = """
        **Authentication & Authorization Endpoints**
        Core authentication endpoints for Microsoft OAuth2 integration, JWT token management, 
        and user session handling.
        """;

    public static final String USER_TAG_DESCRIPTION = """
        **User Profile & Management Endpoints**
        """;

    public static final String PERMISSION_TAG_DESCRIPTION = """
        **Permission Management Endpoints**
        """;

    public static final String ROLE_TAG_DESCRIPTION = """
        **Role Management Endpoints**
        """;

    // =========================== AUTH CONTROLLER ===========================
    public static final class Auth {
        public static final String VERIFY_MICROSOFT_SUMMARY = "Verify Microsoft OAuth2 Token";
        public static final String VERIFY_MICROSOFT_DESCRIPTION = """
                **Complete Microsoft OAuth2 Authentication Flow**
                
                This endpoint completes the OAuth2 authentication process by exchanging the authorization code 
                received from Microsoft for user information and generating JWT tokens.
                """;

        public static final String REFRESH_TOKEN_SUMMARY = "Refresh Access Token";
        public static final String REFRESH_TOKEN_DESCRIPTION = """
                **Obtain New Access Token Using Refresh Token**
                """;

        public static final String LOGOUT_SUMMARY = "User Logout";
        public static final String LOGOUT_DESCRIPTION = """
                **Securely Logout User and Clear Session**
                """;

        // Request/Response Examples
        public static final String VERIFY_REQUEST_EXAMPLE = """
                {
                  "authCode": "0.AXoAuJOzWdFV90CzJx_ZdO0mVgZpGpL_UM-OcE..."
                }
                """;

        public static final String AUTH_RESPONSE_EXAMPLE = """
                {
                  "success": true,
                  "data": {
                    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJ1c2VySWQiOiIxMjM0NTY3OC05YWJjLWRlZjAtMTIzNC01Njc4OWFiY2RlZjAiLCJ0eXBlIjoiQUNDRVNTIiwicm9sZSI6IkFETUlOIiwicGVybWlzc2lvbnMiOlsiVklFV19BRE1JTiIsIkVESVRfQURNSU5fUFJPRklMRSJdLCJpYXQiOjE3MDk2NDAwMDAsImV4cCI6MTcwOTY0MDkwMH0...",
                    "expiresIn": 900,
                    "tokenType": "Bearer",
                    "user": {
                      "email": "user@example.com",
                      "name": "John Doe",
                      "microsoftId": "12345678-9abc-def0-1234-56789abcdef0",
                      "createdAt": "2024-01-15T10:30:00Z",
                      "lastLogin": "2024-03-05T14:22:00Z"
                    }
                  }
                }
                """;
    }

    // =========================== USER CONTROLLER ===========================
    public static final class User {
        public static final String GET_PROFILE_SUMMARY = "Get User Profile";
        public static final String GET_PROFILE_DESCRIPTION = """
                **Retrieve Authenticated User's Profile Information**
                """;

        public static final String TEST_SUPER_ADMIN_SUMMARY = "Test Super Admin Access";
        public static final String TEST_SUPER_ADMIN_DESCRIPTION = """
                **Test Endpoint for Super Admin Role Verification**
                """;

        public static final String PROFILE_RESPONSE_EXAMPLE = """
                {
                  "success": true,
                  "data": {
                    "email": "admin@fundquestnigeria.com",
                    "name": "John Doe",
                    "microsoftId": "12345678-9abc-def0-1234-56789abcdef0",
                    "createdAt": "2024-01-15T10:30:00Z",
                    "lastLogin": "2024-03-05T14:22:00Z"
                  }
                }
                """;
    }

    // =========================== PERMISSION CONTROLLER ===========================
    public static final class Permission {
        public static final String GET_ALL_SUMMARY = "Get All Permissions";
        public static final String GET_ALL_DESCRIPTION = """
                **Retrieve All System Permissions (Super Admin Only)**
                """;

        public static final String GET_ACTIVE_SUMMARY = "Get Active Permissions";
        public static final String GET_ACTIVE_DESCRIPTION = """
                **Retrieve All Active Permissions**
                """;

        public static final String GET_BY_CATEGORY_SUMMARY = "Get Permissions by Category";
        public static final String GET_BY_CATEGORY_DESCRIPTION = """
                **Retrieve Permissions Filtered by Category**
                """;

        public static final String GET_BY_ID_SUMMARY = "Get Permission by ID";
        public static final String GET_BY_ID_DESCRIPTION = """
                **Retrieve Specific Permission Details**
                """;

        public static final String PERMISSIONS_RESPONSE_EXAMPLE = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": 1,
                      "name": "VIEW_ADMIN"
                    },
                    {
                      "id": 2,
                      "name": "EDIT_ADMIN_PROFILE"
                    },
                    {
                      "id": 3,
                      "name": "INVITE_ADMIN"
                    }
                  ]
                }
                """;
    }

    // =========================== ROLE CONTROLLER ===========================
    public static final class Role {
        public static final String GET_ALL_SUMMARY = "Get All Roles";
        public static final String GET_ALL_DESCRIPTION = """
                **Retrieve All System Roles (Super Admin Only)**
                """;

        public static final String GET_ACTIVE_SUMMARY = "Get Active Roles";
        public static final String GET_ACTIVE_DESCRIPTION = """
                **Retrieve All Active Roles**
                """;

        public static final String GET_BY_ID_SUMMARY = "Get Role by ID";
        public static final String GET_BY_ID_DESCRIPTION = """
                **Retrieve Specific Role Details**
                """;

        public static final String ROLES_RESPONSE_EXAMPLE = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": 1,
                      "name": "SUPER_ADMIN"
                    },
                    {
                      "id": 2,
                      "name": "ADMIN"
                    }
                  ]
                }
                """;
    }

    // =========================== COMMON RESPONSES ===========================
    public static final String UNAUTHORIZED_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "UNAUTHORIZED",
                "message": "Invalid or missing authentication token"
              }
            }
            """;

    public static final String FORBIDDEN_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "INSUFFICIENT_PERMISSIONS",
                "message": "User does not have required permissions for this operation"
              }
            }
            """;

    public static final String USER_NOT_INVITED_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "USER_NOT_INVITED",
                "message": "User has not been invited to access this application. Please contact your administrator."
              }
            }
            """;

    public static final String INVALID_TOKEN_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "INVALID_REFRESH_TOKEN",
                "message": "Invalid or expired refresh token"
              }
            }
            """;

    public static final String VALIDATION_ERROR_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "VALIDATION_ERROR",
                "message": "Validation failed: {authCode=Authorization code is required}"
              }
            }
            """;

    public static final String SUCCESS_LOGOUT_RESPONSE = """
            {
              "success": true,
              "message": "Successfully logged out"
            }
            """;
}