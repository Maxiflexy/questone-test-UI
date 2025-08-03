package com.fundquest.auth.service.role;

import com.fundquest.auth.entity.Role;
import com.fundquest.auth.repository.RoleRepository;
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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public List<Role> findAllActive() {
        return roleRepository.findByIsActiveTrue();
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    @Transactional
    public Role create(Role role) {
        if (existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
        }

        Role savedRole = roleRepository.save(role);
        return savedRole;
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}