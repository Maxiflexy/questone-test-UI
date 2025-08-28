package com.fundquest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permission_groups_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PermissionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank(message = "Permission group name cannot be blank")
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "permissionGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    public PermissionGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Helper methods for managing categories
    public void addCategory(Category category) {
        categories.add(category);
        category.setPermissionGroup(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.setPermissionGroup(null);
    }
}