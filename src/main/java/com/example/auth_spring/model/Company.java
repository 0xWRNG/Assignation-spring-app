package com.example.auth_spring.model;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String home_url;


    @Column(length = 15)
    private String phone;


    @Column(length = 64)
    private String email;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;


}
