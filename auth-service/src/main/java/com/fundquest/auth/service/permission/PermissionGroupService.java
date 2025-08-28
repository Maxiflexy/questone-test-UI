package com.fundquest.auth.service.permission;

import com.fundquest.auth.entity.Category;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.PermissionGroup;
import com.fundquest.auth.repository.PermissionGroupRepository;
import com.fundquest.auth.repository.PermissionRepository;
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
public class PermissionGroupService {

    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Find all permission groups
     * @return list of all permission groups
     */
    public List<PermissionGroup> findAll() {
        log.debug("Finding all permission groups");
        return permissionGroupRepository.findAll();
    }

    /**
     * Find all active permission groups
     * @return list of active permission groups
     */
    public List<PermissionGroup> findAllActive() {
        log.debug("Finding all active permission groups");
        return permissionGroupRepository.findByIsActiveTrue();
    }

    /**
     * Find permission group by ID
     * @param id permission group ID
     * @return optional permission group
     */
    public Optional<PermissionGroup> findById(Long id) {
        log.debug("Finding permission group by ID: {}", id);
        return permissionGroupRepository.findById(id);
    }

    /**
     * Find permission group by name
     * @param name permission group name
     * @return optional permission group
     */
    public Optional<PermissionGroup> findByName(String name) {
        log.debug("Finding permission group by name: {}", name);
        return permissionGroupRepository.findByName(name);
    }

    /**
     * Find permission group with categories and permissions
     * @param id permission group ID
     * @return optional permission group with loaded relationships
     */
    public Optional<PermissionGroup> findByIdWithCategoriesAndPermissions(Long id) {
        log.debug("Finding permission group with categories and permissions by ID: {}", id);

        Optional<PermissionGroup> groupOpt = permissionGroupRepository.findByIdWithCategories(id);
        if (groupOpt.isPresent()) {
            PermissionGroup group = groupOpt.get();
            loadPermissionsForCategories(List.of(group));
        }
        return groupOpt;
    }

    /**
     * Find all permission groups with categories and permissions
     * @return list of permission groups with loaded relationships
     */
    public List<PermissionGroup> findAllWithCategoriesAndPermissions() {
        log.debug("Finding all permission groups with categories and permissions");

        List<PermissionGroup> permissionGroups = permissionGroupRepository.findAllWithCategories();
        loadPermissionsForCategories(permissionGroups);
        return permissionGroups;
    }

    /**
     * Helper method to batch load permissions for categories
     * @param permissionGroups list of permission groups with loaded categories
     */
    private void loadPermissionsForCategories(List<PermissionGroup> permissionGroups) {
        // Collect all category IDs
        Set<Long> categoryIds = permissionGroups.stream()
                .flatMap(group -> group.getCategories().stream())
                .map(Category::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (!categoryIds.isEmpty()) {
            // Batch load all permissions
            List<Permission> allPermissions = permissionRepository.findByCategoryIdInAndIsActiveTrue(new ArrayList<>(categoryIds));

            // Group by category ID
            Map<Long, List<Permission>> permissionsByCategory = allPermissions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(permission -> permission.getCategory().getId()));

            // Set permissions for each category
            for (PermissionGroup group : permissionGroups) {
                for (Category category : group.getCategories()) {
                    List<Permission> categoryPermissions = permissionsByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    category.setPermissions(categoryPermissions);
                }
            }
        }
    }

    /**
     * Create new permission group
     * @param permissionGroup permission group to create
     * @return created permission group
     */
    @Transactional
    public PermissionGroup create(PermissionGroup permissionGroup) {
        log.info("Creating new permission group: {}", permissionGroup.getName());

        if (existsByName(permissionGroup.getName())) {
            throw new IllegalArgumentException("Permission group with name '" + permissionGroup.getName() + "' already exists");
        }

        PermissionGroup savedGroup = permissionGroupRepository.save(permissionGroup);
        log.info("Successfully created permission group: {}", savedGroup.getName());
        return savedGroup;
    }

    /**
     * Check if permission group exists by name
     * @param name permission group name
     * @return true if exists, false otherwise
     */
    public boolean existsByName(String name) {
        log.debug("Checking if permission group exists by name: {}", name);
        return permissionGroupRepository.existsByName(name);
    }
}