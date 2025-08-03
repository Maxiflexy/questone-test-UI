package com.fundquest.auth.repository;

import com.fundquest.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByIsActiveTrue();

    List<Permission> findByCategory(String category);

    List<Permission> findByCategoryAndIsActiveTrue(String category);

    boolean existsByName(String name);
}