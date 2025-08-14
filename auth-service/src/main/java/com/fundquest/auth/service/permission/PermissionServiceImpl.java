package com.fundquest.auth.service.permission;

import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.repository.PermissionRepository;
import com.fundquest.auth.util.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public Optional<Permission> findByName(String name) {
        log.debug("Finding permission by name: {}", name);
        return permissionRepository.findByName(name);
    }

    @Override
    public List<Permission> findAllActive() {
        log.debug("Finding all active permissions");
        return permissionRepository.findByIsActiveTrue();
    }

    @Override
    public List<Permission> findByCategory(String category) {
        log.debug("Finding permissions by category: {}", category);
        return permissionRepository.findByCategory(category);
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("Checking if permission exists by name: {}", name);
        return permissionRepository.existsByName(name);
    }

    @Override
    @Transactional
    public Permission create(Permission permission) {
        log.info("Creating new permission: {}", permission.getName());

        if (existsByName(permission.getName())) {
            throw new IllegalArgumentException("Permission with name '" + permission.getName() + "' already exists");
        }

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Successfully created permission: {}", savedPermission.getName());
        return savedPermission;
    }

    @Override
    public Set<Permission> findByNames(Set<String> permissionNames) {
        log.debug("Finding permissions by names: {}", permissionNames);

        if (permissionNames == null || permissionNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Permission> permissions = new HashSet<>();
        for (String name : permissionNames) {
            findByName(name).ifPresent(permissions::add);
        }

        log.debug("Found {} permissions out of {} requested", permissions.size(), permissionNames.size());
        return permissions;
    }

    public Set<Permission> findByIds(List<Long> permissionIds) {
        log.debug("Finding permissions by IDs: {}", permissionIds);

        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        Set<Permission> permissionSet = new HashSet<>(permissions);

        log.debug("Found {} permissions out of {} requested IDs", permissionSet.size(), permissionIds.size());
        return permissionSet;
    }

    @Override
    public boolean existsAllByIds(List<Long> permissionIds) {
        log.debug("Checking if all permission IDs exist: {}", permissionIds);

        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }

        // Use the active permissions count for better validation
        long existingCount = permissionRepository.countByIdInAndIsActiveTrue(permissionIds);
        boolean allExist = existingCount == permissionIds.size();

        log.debug("Checked {} permission IDs, {} active exist", permissionIds.size(), existingCount);
        return allExist;
    }

    @Override
    public List<PermissionResponse> findAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissionMapper.toPermissionResponseList(permissions);
    }

    @Override
    public PermissionResponse findById(Long permissionId) {

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));;

        PermissionResponse permissionResponse = permissionMapper.toPermissionResponse(permission);

        log.info("Successfully retrieved permission: {}", permission.getName());

        return permissionResponse;
    }
}