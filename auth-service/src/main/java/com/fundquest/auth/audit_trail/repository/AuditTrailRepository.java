package com.fundquest.auth.audit_trail.repository;

import com.fundquest.auth.audit_trail.entity.AuditTrail;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.AuditStatus;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    /**
     * Find all audit trails with pagination, ordered by most recent first
     */
    @Query("SELECT a FROM AuditTrail a ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findAllOrderByTimestampDesc(Pageable pageable);

    /**
     * Search audit trails by user email (case-insensitive partial match)
     */
    @Query("SELECT a FROM AuditTrail a WHERE LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByUserEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);

    /**
     * Search audit trails by user name (case-insensitive partial match)
     */
    @Query("SELECT a FROM AuditTrail a WHERE LOWER(a.userName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByUserNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Search audit trails by action description (case-insensitive partial match)
     */
    @Query("SELECT a FROM AuditTrail a WHERE LOWER(a.actionDescription) LIKE LOWER(CONCAT('%', :description, '%')) ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByActionDescriptionContainingIgnoreCase(@Param("description") String description, Pageable pageable);

    /**
     * Filter by action type
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.actionType = :actionType ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByActionType(@Param("actionType") ActionType actionType, Pageable pageable);

    /**
     * Filter by resource type
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.resourceType = :resourceType ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByResourceType(@Param("resourceType") ResourceType resourceType, Pageable pageable);

    /**
     * Filter by status
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.status = :status ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByStatus(@Param("status") AuditStatus status, Pageable pageable);

    /**
     * Filter by date range
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.initiatedDate BETWEEN :startDate AND :endDate ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT a FROM AuditTrail a WHERE " +
            "(:userEmail IS NULL OR LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND " +
            "(:userName IS NULL OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :userName, '%'))) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:startDate IS NULL OR a.initiatedDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.initiatedDate <= :endDate) AND " +
            "(:searchTerm IS NULL OR LOWER(a.actionDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findWithFilters(
            @Param("userEmail") String userEmail,
            @Param("userName") String userName,
            @Param("actionType") ActionType actionType,
            @Param("resourceType") ResourceType resourceType,
            @Param("status") AuditStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find audit trails for a specific user
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.userEmail = :email ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByUserEmail(@Param("email") String email, Pageable pageable);

    /**
     * Find audit trails for a specific resource
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByResource(@Param("resourceType") ResourceType resourceType, @Param("resourceId") String resourceId, Pageable pageable);

    /**
     * Count audit trails by action type
     */
    @Query("SELECT a.actionType, COUNT(a) FROM AuditTrail a GROUP BY a.actionType")
    List<Object[]> countByActionType();

    /**
     * Count audit trails by user for a specific date range
     */
    @Query("SELECT a.userEmail, COUNT(a) FROM AuditTrail a WHERE a.initiatedDate BETWEEN :startDate AND :endDate GROUP BY a.userEmail ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find recent failed audit trails
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.status = 'FAILED' ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findRecentFailures(Pageable pageable);

    /**
     * Find audit trails by service name
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.serviceName = :serviceName ORDER BY a.initiatedTimestamp DESC")
    Page<AuditTrail> findByServiceName(@Param("serviceName") String serviceName, Pageable pageable);
}