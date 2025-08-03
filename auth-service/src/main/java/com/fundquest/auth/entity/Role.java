package com.fundquest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "role_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank(message = "Role name cannot be blank")
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "level")
    private Integer level; // e.g., 1 for SUPER_ADMIN, 2 for ADMIN - for hierarchical purposes

    public Role(String name, String description, Integer level) {
        this.name = name;
        this.description = description;
        this.level = level;
    }
}