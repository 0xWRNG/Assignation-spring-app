package com.example.auth_spring.repository;


import com.example.auth_spring.model.BookedTimeslot;
import com.example.auth_spring.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookedTimeslotRepo extends JpaRepository<BookedTimeslot, Integer> {
    List<BookedTimeslot> findByExecutorToServiceId(Integer executorToServiceId); // Таймслоты для определённой услуги
    List<BookedTimeslot> findByUserId(Integer userId); // Таймслоты, забронированные пользователем

//    List<BookedTimeslot> findByExecutorId(Integer executorId);
//    List<BookedTimeslot> findByExecutorToServiceId(Integer executorToServiceId);
    List<BookedTimeslot> findByExecutorToServiceExecutorIdAndExecutorToServiceProvidedServiceId(Integer executorId, Integer serviceId);
    @Query("""
    SELECT b 
    FROM BookedTimeslot b 
    JOIN ExecutorToService e ON b.executorToService.id = e.id 
    WHERE FUNCTION('DATE', b.timeBegin) = :date 
      AND e.executor.id = :executorId 
    """)
    List<BookedTimeslot> findBookedTimeslotsByDateAndExecutorId(
            @Param("date") LocalDate date,
            @Param("executorId") Integer executorId);

    List<BookedTimeslot> findByExecutorToServiceExecutorCompanyIdAndStatus(Integer companyId, Status status);
}

