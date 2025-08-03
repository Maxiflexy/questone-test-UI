package com.fundquest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 255)
    private String id;

    @Column(name = "microsoft_id", unique = true)
    private String microsoftId;

    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @EqualsAndHashCode.Include
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "preferred_username")
    private String preferredUsername;

    @Column(name = "is_invited")
    @Builder.Default
    private Boolean isInvited = false;

    @Column(name = "is_microsoft_verified")
    @Builder.Default
    private Boolean isMicrosoftVerified = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Direct relationship with permissions for flexibility
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_permission_tbl",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "invited_by")
    private String invitedBy; // Email of who invited this user

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void completeMicrosoftVerification(String microsoftId, String name, String preferredUsername) {
        this.microsoftId = microsoftId;
        this.name = name;
        this.preferredUsername = preferredUsername;
        this.isMicrosoftVerified = true;
        this.updateLastLogin();
    }

    public void invite(String invitedByEmail) {
        this.isInvited = true;
        this.invitedBy = invitedByEmail;
        this.invitedAt = LocalDateTime.now();
    }

    // Permission management methods
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions.clear();
        if (permissions != null) {
            this.permissions.addAll(permissions);
        }
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public boolean isInvited() {
        return Boolean.TRUE.equals(this.isInvited);
    }

    public boolean isMicrosoftVerified() {
        return Boolean.TRUE.equals(this.isMicrosoftVerified);
    }
}