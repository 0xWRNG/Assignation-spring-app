package com.example.auth_spring.repository;


import com.example.auth_spring.model.ExecutorToService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutorToServiceRepo extends JpaRepository<ExecutorToService, Integer> {
    List<ExecutorToService> findByExecutorId(Integer executorId); // Связь по ID исполнителя
    List<ExecutorToService> findByProvidedServiceId(Integer serviceId);// Связь по ID услуги
    void deleteByExecutorIdAndProvidedServiceId(Integer executor_id, Integer providedService_id);
    Optional<ExecutorToService> findByExecutorIdAndProvidedServiceId(Integer executor_id, Integer providedService_id);
    List<ExecutorToService> findByProvidedServiceIdAndProvidedServiceCompanyId(Integer providedService_id, Integer company_id);
    Boolean existsByExecutorIdAndProvidedServiceId(Integer executor_id, Integer providedService_id);
}
