package com.example.auth_spring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "executors_to_service")
public class ExecutorToService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @ManyToOne
    @JoinColumn(name = "executor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Executor executor;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProvidedService providedService;
}
