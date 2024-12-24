package com.example.auth_spring.model;

import com.example.auth_spring.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Data
@Entity
@Table(name = "users")
public class User {

    // Getters and setters
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Column(nullable = false, unique = true, length = 64)
    private String login;

    @Getter
    @Column(nullable = false, length = 256)
    private String password;

    @Getter
    @Column(length = 128)
    private String name;

    @Getter
    @Column(length = 128)
    private String surname;

    @Getter
    @Column(length = 128)
    private String patronymic;

    @Email
    @Getter
    @Column(length = 64)
    private String email;

    @Getter
    @Column(length = 15)
    private String phone;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private Role role = Role.USER;

    public String getRole() {
        return role.toString();
    }

    public void setRole(String role) {
        switch (role) {
            case "MANAGER":
                this.role = Role.MANAGER;
                break;
            default:
                this.role = Role.USER;
                break;

        }
    }

}
