package com.fundquest.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private String microsoftId;
    private String givenName;
    private String familyName;
    private String jobTitle;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;


    public UserResponse(UUID id, String email, String name, String microsoftId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.microsoftId = microsoftId;
    }

}