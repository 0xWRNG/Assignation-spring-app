package com.example.auth_spring.repository;


import com.example.auth_spring.model.ProvidedService;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProvidedServiceRepo extends JpaRepository<ProvidedService, Integer> {
    List<ProvidedService> findByCompanyId(Integer companyId);
    List<ProvidedService> findByTitleContainingIgnoreCase(String substring);


    @Query("SELECT p FROM ProvidedService p WHERE UPPER(CAST(p.description AS string)) LIKE UPPER(CONCAT('%', :description, '%'))")
    List<ProvidedService> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    Optional<ProvidedService> findById(@NonNull Integer serviceId);
}
