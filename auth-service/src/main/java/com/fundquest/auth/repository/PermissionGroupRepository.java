package com.fundquest.auth.repository;

import com.fundquest.auth.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {

    Optional<PermissionGroup> findByName(String name);

    List<PermissionGroup> findByIsActiveTrue();

    boolean existsByName(String name);

    /**
     * Find all permission groups with their categories (step 1 of hierarchical loading)
     * Note: We fetch categories first, then permissions separately to avoid MultipleBagFetchException
     */
    @Query("SELECT DISTINCT pg FROM PermissionGroup pg " +
            "LEFT JOIN FETCH pg.categories c " +
            "WHERE pg.isActive = true AND c.isActive = true " +
            "ORDER BY pg.name, c.name")
    List<PermissionGroup> findAllWithCategories();

    /**
     * Find specific permission group with its categories (step 1 of hierarchical loading)
     */
    @Query("SELECT DISTINCT pg FROM PermissionGroup pg " +
            "LEFT JOIN FETCH pg.categories c " +
            "WHERE pg.id = :id AND pg.isActive = true AND c.isActive = true")
    Optional<PermissionGroup> findByIdWithCategories(Long id);
}