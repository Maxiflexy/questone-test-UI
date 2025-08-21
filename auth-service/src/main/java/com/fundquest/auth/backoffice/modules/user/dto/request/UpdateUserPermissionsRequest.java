package com.fundquest.auth.backoffice.modules.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPermissionsRequest {

    @NotNull(message = "Permission names are required")
    @NotEmpty(message = "At least one permission name must be provided")
    private List<String> permissionNames;
}