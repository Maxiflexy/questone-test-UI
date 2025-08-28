package com.fundquest.auth.service.permission;

import com.fundquest.auth.entity.Category;
import com.fundquest.auth.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Find all categories
     * @return list of all categories
     */
    public List<Category> findAll() {
        log.debug("Finding all categories");
        return categoryRepository.findAll();
    }

    /**
     * Find all active categories
     * @return list of active categories
     */
    public List<Category> findAllActive() {
        log.debug("Finding all active categories");
        return categoryRepository.findByIsActiveTrue();
    }

    /**
     * Find category by ID
     * @param id category ID
     * @return optional category
     */
    public Optional<Category> findById(Long id) {
        log.debug("Finding category by ID: {}", id);
        return categoryRepository.findById(id);
    }

    /**
     * Find category by name
     * @param name category name
     * @return optional category
     */
    public Optional<Category> findByName(String name) {
        log.debug("Finding category by name: {}", name);
        return categoryRepository.findByName(name);
    }

    /**
     * Find categories by permission group ID
     * @param permissionGroupId permission group ID
     * @return list of categories
     */
    public List<Category> findByPermissionGroupId(Long permissionGroupId) {
        log.debug("Finding categories by permission group ID: {}", permissionGroupId);
        return categoryRepository.findByPermissionGroupIdAndIsActiveTrue(permissionGroupId);
    }

    /**
     * Find category with permissions
     * @param id category ID
     * @return optional category with loaded permissions
     */
    public Optional<Category> findByIdWithPermissions(Long id) {
        log.debug("Finding category with permissions by ID: {}", id);
        return categoryRepository.findByIdWithPermissions(id);
    }

    /**
     * Find categories with permissions for a permission group
     * @param permissionGroupId permission group ID
     * @return list of categories with loaded permissions
     */
    public List<Category> findByPermissionGroupIdWithPermissions(Long permissionGroupId) {
        log.debug("Finding categories with permissions by permission group ID: {}", permissionGroupId);
        return categoryRepository.findByPermissionGroupIdWithPermissions(permissionGroupId);
    }

    /**
     * Create new category
     * @param category category to create
     * @return created category
     */
    @Transactional
    public Category create(Category category) {
        log.info("Creating new category: {}", category.getName());

        if (existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Successfully created category: {}", savedCategory.getName());
        return savedCategory;
    }

    /**
     * Check if category exists by name
     * @param name category name
     * @return true if exists, false otherwise
     */
    public boolean existsByName(String name) {
        log.debug("Checking if category exists by name: {}", name);
        return categoryRepository.existsByName(name);
    }
}