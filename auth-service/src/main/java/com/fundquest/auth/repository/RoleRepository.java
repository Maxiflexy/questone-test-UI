package com.fundquest.auth.repository;

import com.fundquest.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByIsActiveTrue();

    List<Role> findByOrderByLevelAsc(); // For hierarchical roles

    boolean existsByName(String name);
}