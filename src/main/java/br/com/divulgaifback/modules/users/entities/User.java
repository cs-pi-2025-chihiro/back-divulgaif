package br.com.divulgaifback.modules.users.entities;

import br.com.divulgaifback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {
    private static final int MAXIMUM_BIO_VALUE = 500;

    @Column(name = "name")
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "secondary_email")
    private String secondaryEmail;

    @Column(unique = true, length = 20)
    private String cpf;

    @Column(name = "rg", unique = true, length = 100)
    private String rg;

    @Column(name = "ra", unique = true, length = 50)
    private String ra;

    @Column(name = "password")
    private String password;

    @Column(name = "bio", length = MAXIMUM_BIO_VALUE)
    private String bio;

    @Column(name = "phone")
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email_confirmed_at")
    private LocalDateTime emailConfirmedAt;

    @Column(name = "forgot_password_token")
    private String forgotPasswordToken;

    @Column(name = "user_type")
    private String userType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}