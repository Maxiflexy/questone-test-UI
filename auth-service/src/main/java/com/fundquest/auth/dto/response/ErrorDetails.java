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
public class ErrorDetails {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String code;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String message;
}