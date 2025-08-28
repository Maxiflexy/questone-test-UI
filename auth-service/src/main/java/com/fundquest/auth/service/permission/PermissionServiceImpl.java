package com.fundquest.auth.service.permission;

import com.fundquest.auth.dto.response.DetailedPermissionResponse;
import com.fundquest.auth.dto.response.HierarchicalPermissionResponse;
import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Category;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.PermissionGroup;
import com.fundquest.auth.repository.CategoryRepository;
import com.fundquest.auth.repository.PermissionGroupRepository;
import com.fundquest.auth.repository.PermissionRepository;
import com.fundquest.auth.util.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final CategoryRepository categoryRepository;
    private final PermissionMapper permissionMapper;

    // ===== Permission Group Operations =====

    @Override
    public HierarchicalPermissionResponse findAllHierarchical() {
        log.debug("Finding all permissions with hierarchical structure");

        // Step 1: Fetch permission groups with categories (avoids MultipleBagFetchException)
        List<PermissionGroup> permissionGroups = permissionGroupRepository.findAllWithCategories();

        // Step 2: Batch load all permissions for all categories to minimize queries
        Set<Long> categoryIds = permissionGroups.stream()
                .flatMap(group -> group.getCategories().stream())
                .map(Category::getId)
                .collect(Collectors.toSet());

        if (!categoryIds.isEmpty()) {
            // Fetch all permissions for all categories in one query
            List<Permission> allPermissions = permissionRepository.findByCategoryIdInAndIsActiveTrue(new ArrayList<>(categoryIds));

            // Group permissions by category ID
            Map<Long, List<Permission>> permissionsByCategory = allPermissions.stream()
                    .collect(Collectors.groupingBy(permission -> permission.getCategory().getId()));

            // Set permissions for each category
            for (PermissionGroup group : permissionGroups) {
                for (Category category : group.getCategories()) {
                    List<Permission> categoryPermissions = permissionsByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    category.setPermissions(categoryPermissions);
                }
            }
        }

        return permissionMapper.toHierarchicalPermissionResponse(permissionGroups);
    }

    @Override
    public Optional<PermissionGroup> findPermissionGroupById(Long id) {
        log.debug("Finding permission group by ID: {}", id);
        return permissionGroupRepository.findById(id);
    }

    @Override
    public Optional<PermissionGroup> findPermissionGroupByName(String name) {
        log.debug("Finding permission group by name: {}", name);
        return permissionGroupRepository.findByName(name);
    }

    @Override
    public List<PermissionGroup> findAllActivePermissionGroups() {
        log.debug("Finding all active permission groups");
        return permissionGroupRepository.findByIsActiveTrue();
    }

    // ===== Category Operations =====

    @Override
    public Optional<Category> findCategoryById(Long id) {
        log.debug("Finding category by ID: {}", id);
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findCategoryByName(String name) {
        log.debug("Finding category by name: {}", name);
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> findCategoriesByPermissionGroupId(Long permissionGroupId) {
        log.debug("Finding categories by permission group ID: {}", permissionGroupId);
        return categoryRepository.findByPermissionGroupIdAndIsActiveTrue(permissionGroupId);
    }

    // ===== Permission Operations =====

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
    public List<Permission> findByCategoryId(Long categoryId) {
        log.debug("Finding permissions by category ID: {}", categoryId);
        return permissionRepository.findByCategoryIdAndIsActiveTrue(categoryId);
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

    @Override
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
        log.debug("Finding all permissions (flat structure for backwards compatibility)");
        List<Permission> permissions = permissionRepository.findAll();
        return permissionMapper.toPermissionResponseList(permissions);
    }

    @Override
    public PermissionResponse findById(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public DetailedPermissionResponse findByIdDetailed(Long permissionId) {
        Permission permission = permissionRepository.findByIdWithCategoryAndGroup(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));
        return permissionMapper.toDetailedPermissionResponse(permission);
    }
}