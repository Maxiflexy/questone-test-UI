package com.fundquest.auth.entity.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {

    // Admin Management Permissions
    INVITE_ADMIN("INVITE_ADMIN", "Invite Admin", "ADMIN_MANAGEMENT"),
    VIEW_OTHER_ADMIN_USERS("VIEW_OTHER_ADMIN_USERS", "View other Admin Users", "ADMIN_MANAGEMENT"),
    EDIT_ADMIN_PROFILE("EDIT_ADMIN_PROFILE", "Edit an Admin Profile", "ADMIN_MANAGEMENT"),
    VIEW_ADMIN("VIEW_ADMIN", "View Admin", "ADMIN_MANAGEMENT"),
    ASSIGN_AND_UNASSIGN("ASSIGN_AND_UNASSIGN", "Assign and unassign", "ADMIN_MANAGEMENT"),
    DISABLE_ENABLE_ADMIN("DISABLE_ENABLE_ADMIN", "Disable and enable an Admin", "ADMIN_MANAGEMENT"),
    APPROVE_DECLINE_ADMIN_REQUEST("APPROVE_DECLINE_ADMIN_REQUEST", "Approve or decline an Admin request", "ADMIN_MANAGEMENT"),

    // Customer Management Permissions
    VIEW_CUSTOMERS("VIEW_CUSTOMERS", "View Customers", "CUSTOMER_MANAGEMENT"),
    EDIT_CUSTOMER_PROFILE("EDIT_CUSTOMER_PROFILE", "Edit a Customer profile", "CUSTOMER_MANAGEMENT"),
    RESET_CUSTOMER_PASSWORD("RESET_CUSTOMER_PASSWORD", "Reset a customer's password", "CUSTOMER_MANAGEMENT"),
    DISABLE_ENABLE_CUSTOMER("DISABLE_ENABLE_CUSTOMER", "Disable and enable a customer", "CUSTOMER_MANAGEMENT");

    private final String name;
    private final String description;
    private final String category;

    PermissionEnum(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
}
