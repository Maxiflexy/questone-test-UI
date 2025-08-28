package com.fundquest.auth.entity.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {

    // Main Admin Permissions
    VIEW_OTHER_ADMIN_USERS("VIEW_OTHER_ADMIN_USERS", "Can view other Admin Users", "MAIN_ADMIN_PERMISSIONS"),
    INVITE_ADMIN("INVITE_ADMIN", "Can Invite Admin", "MAIN_ADMIN_PERMISSIONS"),
    VIEW_ADMIN_PERMISSIONS("VIEW_ADMIN_PERMISSIONS", "Can view Admin Permissions", "MAIN_ADMIN_PERMISSIONS"),
    INITIATE_PERMISSION_ASSIGNMENT("INITIATE_PERMISSION_ASSIGNMENT", "Can initiate the assignment and unassignment of permissions", "MAIN_ADMIN_PERMISSIONS"),
    APPROVE_PERMISSION_ASSIGNMENT("APPROVE_PERMISSION_ASSIGNMENT", "Can review and approve permission assignment and unassignment requests", "MAIN_ADMIN_PERMISSIONS"),
    APPROVE_ADMIN_STATUS_CHANGE("APPROVE_ADMIN_STATUS_CHANGE", "Can review and approve requests to enable or disable an Admin account", "MAIN_ADMIN_PERMISSIONS"),

    // Other Admin Permissions
    INITIATE_ADMIN_STATUS_CHANGE("INITIATE_ADMIN_STATUS_CHANGE", "Can initiate the request to enable or disable an Admin account", "OTHER_ADMIN_PERMISSIONS"),
    INITIATE_PERMISSION_CREATION("INITIATE_PERMISSION_CREATION", "Can initiate permission creation", "OTHER_ADMIN_PERMISSIONS"),

    // Customer Management Permissions
    CREATE_CUSTOMER("CREATE_CUSTOMER", "Can Create a Customer", "CUSTOMERS"),
    VIEW_CUSTOMER_PROFILES("VIEW_CUSTOMER_PROFILES", "View Customer Profiles", "CUSTOMERS"),
    INITIATE_CUSTOMER_PROFILE_UPDATE("INITIATE_CUSTOMER_PROFILE_UPDATE", "Can initiate a customer profile update (Can edit address, update doc, update tier, BVN, etc)", "CUSTOMERS"),
    APPROVE_CUSTOMER_PROFILE_UPDATE("APPROVE_CUSTOMER_PROFILE_UPDATE", "Can review/approve a customer profile update (Can edit address, update doc, BVN, etc)", "CUSTOMERS"),
    INITIATE_CUSTOMER_PASSWORD_RESET("INITIATE_CUSTOMER_PASSWORD_RESET", "Can initiate a Customer reset Password", "CUSTOMERS"),
    APPROVE_CUSTOMER_PASSWORD_RESET("APPROVE_CUSTOMER_PASSWORD_RESET", "Can approve a Customer reset Password", "CUSTOMERS"),
    INITIATE_CUSTOMER_STATUS_CHANGE("INITIATE_CUSTOMER_STATUS_CHANGE", "Can initiate a request to disable or enable a customer account", "CUSTOMERS"),
    APPROVE_CUSTOMER_STATUS_CHANGE("APPROVE_CUSTOMER_STATUS_CHANGE", "Can review and approve customer disable/enable requests", "CUSTOMERS"),
    INITIATE_DEVICE_UNASSIGNMENT("INITIATE_DEVICE_UNASSIGNMENT", "Can initiate unassigning a device from a customer account", "CUSTOMERS"),
    APPROVE_DEVICE_UNASSIGNMENT("APPROVE_DEVICE_UNASSIGNMENT", "Can approve the unassignment of a device from a customer account", "CUSTOMERS"),

    // Wallet and Transactions Activities Permissions
    VIEW_CUSTOMER_WALLETS_TRANSACTIONS("VIEW_CUSTOMER_WALLETS_TRANSACTIONS", "View Customer Wallets and Transactions logs", "WALLET_AND_TRANSACTIONS"),
    INITIATE_CUSTOMER_CREDIT_DEBIT("INITIATE_CUSTOMER_CREDIT_DEBIT", "Can Initiate a Credit or Debit a Customer Account", "WALLET_AND_TRANSACTIONS"),
    APPROVE_CUSTOMER_CREDIT_DEBIT("APPROVE_CUSTOMER_CREDIT_DEBIT", "Can review or approve a credit or debit to a Customer Account", "WALLET_AND_TRANSACTIONS"),
    PUT_CUSTOMER_ACCOUNT_PND("PUT_CUSTOMER_ACCOUNT_PND", "Can put a Customer Account on PND", "WALLET_AND_TRANSACTIONS"),
    UPDATE_CUSTOMER_MIN_BALANCE("UPDATE_CUSTOMER_MIN_BALANCE", "Can update a Customer Account Minimum Balance", "WALLET_AND_TRANSACTIONS"),
    DOWNLOAD_CUSTOMER_STATEMENT("DOWNLOAD_CUSTOMER_STATEMENT", "Can download a Customer statement of Account", "WALLET_AND_TRANSACTIONS"),

    // Retail Loans Permissions
    VIEW_CUSTOMER_LOAN_HISTORY("VIEW_CUSTOMER_LOAN_HISTORY", "Can view a Customer Loan History", "RETAIL_LOANS"),
    APPROVE_CUSTOMER_LOAN("APPROVE_CUSTOMER_LOAN", "Can approve a Customer Loan", "RETAIL_LOANS"),
    INITIATE_MANUAL_LOAN_CREDIT_DEBIT("INITIATE_MANUAL_LOAN_CREDIT_DEBIT", "Can initiate a manual Loan Credit or Debit", "RETAIL_LOANS"),
    APPROVE_MANUAL_LOAN_CREDIT_DEBIT("APPROVE_MANUAL_LOAN_CREDIT_DEBIT", "Can approve a manual loan, Credit, or Debit", "RETAIL_LOANS"),

    // Cards Permissions
    VIEW_CUSTOMER_CARD_HISTORY("VIEW_CUSTOMER_CARD_HISTORY", "Can view a Customer Card history", "CARDS"),
    INITIATE_CARD_ACTIVATION_DEACTIVATION("INITIATE_CARD_ACTIVATION_DEACTIVATION", "Can initiate a card activation or deactivation", "CARDS"),
    APPROVE_CARD_ACTIVATION_DEACTIVATION("APPROVE_CARD_ACTIVATION_DEACTIVATION", "Can approve a card activation or deactivation", "CARDS"),

    // Investments/Deposits Permissions
    VIEW_CUSTOMER_INVESTMENTS("VIEW_CUSTOMER_INVESTMENTS", "Can view Customer Investments", "INVESTMENTS_DEPOSITS"),
    INITIATE_INVESTMENT_DEBIT("INITIATE_INVESTMENT_DEBIT", "Can Initiate a Debit for a Customer Investment", "INVESTMENTS_DEPOSITS"),
    APPROVE_INVESTMENT_DEBIT("APPROVE_INVESTMENT_DEBIT", "Can approve a Debit for a Customer Investment", "INVESTMENTS_DEPOSITS");

    private final String name;
    private final String description;
    private final String categoryKey;

    PermissionEnum(String name, String description, String categoryKey) {
        this.name = name;
        this.description = description;
        this.categoryKey = categoryKey;
    }
}