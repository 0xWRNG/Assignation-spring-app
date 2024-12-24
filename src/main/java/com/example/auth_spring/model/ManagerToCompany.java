package com.example.auth_spring.model;


import com.example.auth_spring.model.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import org.apache.catalina.Manager;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Data

@Entity
@Table(name = "managers_to_company")
public class ManagerToCompany {

    public ManagerToCompany(User manager, Company company) {
        this.manager = manager;
        this.company = company;
    }
    public ManagerToCompany() {

    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company company;

    @Setter
    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User manager;



}
