package com.fundquest.auth.repository;

import com.fundquest.auth.entity.User;
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
}