package com.fundquest.auth.repository;

import com.fundquest.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByIsActiveTrue();

    List<Permission> findByCategoryId(Long categoryId);

    List<Permission> findByCategoryIdAndIsActiveTrue(Long categoryId);

    /**
     * Find active permissions by multiple category IDs (for batch loading)
     */
    @Query("SELECT p FROM Permission p WHERE p.category.id IN :categoryIds AND p.isActive = true ORDER BY p.category.id, p.name")
    List<Permission> findByCategoryIdInAndIsActiveTrue(@Param("categoryIds") List<Long> categoryIds);

    boolean existsByName(String name);

    /**
     * Check if all permissions with given IDs exist and are active
     * @param ids list of permission IDs
     * @return count of active permissions with given IDs
     */
    long countByIdInAndIsActiveTrue(List<Long> ids);

    /**
     * Find permissions by category name
     */
    @Query("SELECT p FROM Permission p JOIN p.category c WHERE c.name = :categoryName AND p.isActive = true")
    List<Permission> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Find permissions by permission group ID
     */
    @Query("SELECT p FROM Permission p JOIN p.category c JOIN c.permissionGroup pg " +
            "WHERE pg.id = :permissionGroupId AND p.isActive = true")
    List<Permission> findByPermissionGroupId(@Param("permissionGroupId") Long permissionGroupId);

    /**
     * Find permissions with their category and permission group information
     * Useful for building hierarchical responses
     */
    @Query("SELECT p FROM Permission p " +
            "JOIN FETCH p.category c " +
            "JOIN FETCH c.permissionGroup pg " +
            "WHERE p.isActive = true AND c.isActive = true AND pg.isActive = true " +
            "ORDER BY pg.name, c.name, p.name")
    List<Permission> findAllWithCategoryAndGroup();

    /**
     * Find permission by ID with category and permission group
     */
    @Query("SELECT p FROM Permission p " +
            "JOIN FETCH p.category c " +
            "JOIN FETCH c.permissionGroup pg " +
            "WHERE p.id = :id")
    Optional<Permission> findByIdWithCategoryAndGroup(@Param("id") Long id);
}