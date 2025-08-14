package com.fundquest.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorDetails error;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
            * Create successful response with both data and message
     * @param data response data
     * @param message success message
     * @param <T> data type
     * @return ApiResponse with success=true, data, and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }


    /**
     * Create error response with error code and message
     * @param code error code
     * @param message error message
     * @param <T> data type (will be null)
     * @return ApiResponse with success=false and error details
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }

    /**
     * Create error response with error details only
     * @param errorDetails error details object
     * @param <T> data type (will be null)
     * @return ApiResponse with success=false and error details
     */
    public static <T> ApiResponse<T> error(ErrorDetails errorDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(errorDetails)
                .build();
    }

    /**
     * Create error response with error code, message, and additional context message
     * @param code error code
     * @param errorMessage error message
     * @param contextMessage additional context message
     * @param <T> data type (will be null)
     * @return ApiResponse with success=false, error details, and message
     */
    public static <T> ApiResponse<T> error(String code, String errorMessage, String contextMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(errorMessage)
                        .build())
                .message(contextMessage)
                .build();
    }
}