package com.fundquest.auth.entity.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    SUPER_ADMIN("SUPER_ADMIN", "Super Administrator"),
    ADMIN("ADMIN", "Administrator");

    private final String name;
    private final String description;

    RoleEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
