package com.fundquest.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMicrosoftTokenRequest {

    @NotBlank(message = "Auth code is required")
    private String authCode;
}