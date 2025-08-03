package com.fundquest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "permission_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank(message = "Permission name cannot be blank")
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category; // e.g., "ADMIN_MANAGEMENT", "CUSTOMER_MANAGEMENT"

    public Permission(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
}