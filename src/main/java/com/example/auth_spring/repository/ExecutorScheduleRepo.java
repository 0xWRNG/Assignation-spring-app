package com.example.auth_spring.repository;


import com.example.auth_spring.model.ExecutorSchedule;
import com.example.auth_spring.model.enums.Weekday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutorScheduleRepo extends JpaRepository<ExecutorSchedule, Integer> {
    List<ExecutorSchedule> findByExecutorId(Integer executorId);

    void deleteByExecutorIdAndWeekday(Integer executorId, Weekday weekday);
    Optional<ExecutorSchedule> findByExecutorIdAndWeekday(Integer executorId, Weekday weekday);

}
