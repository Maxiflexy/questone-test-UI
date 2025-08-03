//package com.fundquest.auth.config;
//
//import com.fundquest.auth.constants.AppConstants;
//import com.fundquest.auth.entity.Permission;
//import com.fundquest.auth.entity.Role;
//import com.fundquest.auth.entity.enums.PermissionEnum;
//import com.fundquest.auth.entity.enums.RoleEnum;
//import com.fundquest.auth.service.UserService;
//import com.fundquest.auth.service.permission.PermissionService;
//import com.fundquest.auth.service.role.RoleService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final PermissionService permissionService;
//    private final RoleService roleService;
//    private final UserService userService;
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        log.info("Initializing default data...");
//
//        initializePermissions();
//        initializeRoles();
//        initializeSuperAdminUser();
//
//        log.info("Default data initialization completed");
//    }
//
//    private void initializePermissions() {
//        log.info("Initializing permissions...");
//
//        List<PermissionEnum> permissions = Arrays.asList(PermissionEnum.values());
//
//        for (PermissionEnum permissionEnum : permissions) {
//            if (!permissionService.existsByName(permissionEnum.getName())) {
//
//                String category = determinePermissionCategory(permissionEnum);
//
//                Permission permission = Permission.builder()
//                        .name(permissionEnum.getName())
//                        .description(permissionEnum.getDescription())
//                        .category(category)
//                        .build();
//
//                permissionService.create(permission);
//                log.info("Created permission: {} in category: {}", permissionEnum.getName(), category);
//            }
//        }
//
//        log.info("Permissions initialization completed");
//    }
//
//    private String determinePermissionCategory(PermissionEnum permissionEnum) {
//        String name = permissionEnum.getName();
//        if (name.contains("ADMIN")) {
//            return "ADMIN_MANAGEMENT";
//        } else if (name.contains("CUSTOMER")) {
//            return "CUSTOMER_MANAGEMENT";
//        } else {
//            return "GENERAL";
//        }
//    }
//
//    private void initializeRoles() {
//        log.info("Initializing roles...");
//
//        // Create SUPER_ADMIN role
//        if (!roleService.existsByName(RoleEnum.SUPER_ADMIN.getName())) {
//            Role superAdminRole = Role.builder()
//                    .name(RoleEnum.SUPER_ADMIN.getName())
//                    .description(RoleEnum.SUPER_ADMIN.getDescription())
//                    .level(1) // Highest level
//                    .build();
//
//            roleService.create(superAdminRole);
//            log.info("Created role: {}", RoleEnum.SUPER_ADMIN.getName());
//        }
//
//        // Create ADMIN role
//        if (!roleService.existsByName(RoleEnum.ADMIN.getName())) {
//            Role adminRole = Role.builder()
//                    .name(RoleEnum.ADMIN.getName())
//                    .description(RoleEnum.ADMIN.getDescription())
//                    .level(2) // Lower level
//                    .build();
//
//            roleService.create(adminRole);
//            log.info("Created role: {}", RoleEnum.ADMIN.getName());
//        }
//
//        log.info("Roles initialization completed");
//    }
//
//    private void initializeSuperAdminUser() {
//        log.info("Initializing super admin user...");
//
//        if (!userService.existsByEmail(AppConstants.DEFAULT_SUPER_ADMIN_EMAIL)) {
//            Role superAdminRole = roleService.findByName(RoleEnum.SUPER_ADMIN.getName())
//                    .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));
//
//            // Get all permission names for super admin
//            Set<String> allPermissionNames = Arrays.stream(PermissionEnum.values())
//                    .map(PermissionEnum::getName)
//                    .collect(Collectors.toSet());
//
//            userService.inviteUser(
//                    AppConstants.DEFAULT_SUPER_ADMIN_EMAIL,
//                    superAdminRole,
//                    allPermissionNames,
//                    "SYSTEM" // Invited by system
//            );
//
//            log.info("Created super admin user: {} with {} permissions",
//                    AppConstants.DEFAULT_SUPER_ADMIN_EMAIL, allPermissionNames.size());
//        } else {
//            log.info("Super admin user already exists: {}", AppConstants.DEFAULT_SUPER_ADMIN_EMAIL);
//        }
//
//        log.info("Super admin user initialization completed");
//    }
//}
