package com.fundquest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Category name cannot be blank")
    @EqualsAndHashCode.Include
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perm_group_id", nullable = false)
    private PermissionGroup permissionGroup;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

    public Category(String name, PermissionGroup permissionGroup) {
        this.name = name;
        this.permissionGroup = permissionGroup;
    }

    // Helper methods for managing permissions
    public void addPermission(Permission permission) {
        permissions.add(permission);
        permission.setCategory(this);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.setCategory(null);
    }

    // Setter for permissions (needed for manual loading in service)
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }
}