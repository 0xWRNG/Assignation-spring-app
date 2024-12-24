package com.example.auth_spring.repository;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CompanyRepo extends JpaRepository<Company, Integer> {
    List<Company> findByCreatedBy(User user); // Компании, созданные конкретным пользователем
    Set<Company> findByNameContainingIgnoreCase(String substring);
    @Query("SELECT p FROM Company p WHERE UPPER(CAST(p.description AS string)) LIKE UPPER(CONCAT('%', :description, '%'))")
    Set<Company> findByDescriptionContainingIgnoreCase(@Param("description") String description); // Компании, созданные конкретным пользователем
}