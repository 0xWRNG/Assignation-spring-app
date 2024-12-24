package com.example.auth_spring.model;

import com.example.auth_spring.model.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "booked_timeslots")
public class BookedTimeslot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "executor_to_serv_id", nullable = false)
    private ExecutorToService executorToService;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private LocalDateTime timeBegin;
    @Column
    private LocalDateTime timeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

//    public Integer getId(){return id;}
}
