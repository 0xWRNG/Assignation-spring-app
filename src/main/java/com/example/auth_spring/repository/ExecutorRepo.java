package com.example.auth_spring.repository;

import com.example.auth_spring.model.Executor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExecutorRepo extends JpaRepository<Executor, Integer> {
    List<Executor> findByCompanyId(Integer companyId);

    @Query("SELECT p FROM Executor p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :substring, '%')) " +
            "OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :substring, '%')) " +
            "OR LOWER(p.patronymic) LIKE LOWER(CONCAT('%', :substring, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :substring, '%'))")
    List<Executor> findBySubstringInAttributes(@Param("substring") String substring);
}
