package com.fundquest.auth.repository;

import com.fundquest.auth.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByIsActiveTrue();

    List<Category> findByPermissionGroupId(Long permissionGroupId);

    List<Category> findByPermissionGroupIdAndIsActiveTrue(Long permissionGroupId);

    boolean existsByName(String name);

    /**
     * Find categories with their permissions for a specific permission group
     */
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.permissions p " +
            "WHERE c.permissionGroup.id = :permissionGroupId " +
            "AND c.isActive = true AND p.isActive = true " +
            "ORDER BY c.name, p.name")
    List<Category> findByPermissionGroupIdWithPermissions(@Param("permissionGroupId") Long permissionGroupId);

    /**
     * Find category with its permissions
     */
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.permissions p " +
            "WHERE c.id = :id AND c.isActive = true AND p.isActive = true")
    Optional<Category> findByIdWithPermissions(@Param("id") Long id);
}