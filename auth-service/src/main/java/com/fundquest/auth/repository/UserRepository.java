package com.fundquest.auth.repository;

import com.fundquest.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMicrosoftId(String microsoftId);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH u.permissions p WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailWithRoleAndPermissions(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH u.permissions p WHERE u.id = :userId AND u.isActive = true")
    Optional<User> findByIdWithRoleAndPermissions(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH u.permissions p WHERE u.microsoftId = :microsoftId")
    Optional<User> findByMicrosoftIdWithRoleAndPermissions(@Param("microsoftId") String microsoftId);

    boolean existsByEmail(String email);

    boolean existsByMicrosoftId(String microsoftId);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.email = :email")
    void updateLastLoginByEmail(@Param("email") String email, @Param("lastLogin") LocalDateTime lastLogin);



    /**
     * Find all users with pagination, ordered by creation date descending
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r ORDER BY u.createdAt DESC")
    Page<User> findAllUsersWithRole(Pageable pageable);

    /**
     * Search users by name (case-insensitive partial match)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY u.createdAt DESC")
    Page<User> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Filter users by active status
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE u.isActive = :isActive ORDER BY u.createdAt DESC")
    Page<User> findByIsActive(@Param("isActive") boolean isActive, Pageable pageable);

    /**
     * Advanced filter by name and active status
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE " +
            "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive) " +
            "ORDER BY u.createdAt DESC")
    Page<User> findByNameAndStatus(@Param("name") String name, @Param("isActive") Boolean isActive, Pageable pageable);

    /**
     * Count users by active status
     */
    long countByIsActive(boolean isActive);

    /**
     * Search users by email (case-insensitive partial match)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY u.createdAt DESC")
    Page<User> findByEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.name = :roleName ORDER BY u.createdAt DESC")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

}