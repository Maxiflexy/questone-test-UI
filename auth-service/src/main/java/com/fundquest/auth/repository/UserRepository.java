package com.fundquest.auth.repository;

import com.fundquest.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     * @param email the email address
     * @return Optional User
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by Microsoft ID
     * @param microsoftId the Microsoft ID
     * @return Optional User
     */
    Optional<User> findByMicrosoftId(String microsoftId);

    /**
     * Find user by email and check if active
     * @param email the email address
     * @return Optional User
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Find user by Microsoft ID and check if active
     * @param microsoftId the Microsoft ID
     * @return Optional User
     */
    Optional<User> findByMicrosoftIdAndIsActiveTrue(String microsoftId);

    /**
     * Check if user exists by email
     * @param email the email address
     * @return boolean
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by Microsoft ID
     * @param microsoftId the Microsoft ID
     * @return boolean
     */
    boolean existsByMicrosoftId(String microsoftId);

    /**
     * Find user by email or Microsoft ID
     * @param email the email address
     * @param microsoftId the Microsoft ID
     * @return Optional User
     */
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.microsoftId = :microsoftId")
    Optional<User> findByEmailOrMicrosoftId(@Param("email") String email, @Param("microsoftId") String microsoftId);

    /**
     * Count total active users
     * @return long count
     */
    long countByIsActiveTrue();

    /**
     * Find user by preferred username
     * @param preferredUsername the preferred username
     * @return Optional User
     */
    Optional<User> findByPreferredUsername(String preferredUsername);
}