package com.example.auth_spring.model;


import com.example.auth_spring.model.enums.Weekday;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "executor_schedules")
public class ExecutorSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Add this annotation
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "executor_id", nullable = false)
    private Executor executor;

    @Column
    private LocalTime timeBegin;

    @Column
    private LocalTime timeEnd;

    @Column
    @Enumerated(EnumType.STRING)
    private Weekday weekday;


}
