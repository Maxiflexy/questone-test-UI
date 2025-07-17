package com.fundquest.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MicrosoftTokenRequest {

    @NotNull(message = "ID token is required")
    @NotBlank(message = "ID token cannot be blank")
    private String idToken;

}