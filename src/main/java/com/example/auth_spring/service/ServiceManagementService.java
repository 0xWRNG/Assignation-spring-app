package com.example.auth_spring.service;

import com.example.auth_spring.model.ProvidedService;
import com.example.auth_spring.repository.ProvidedServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ServiceManagementService {
    private final ProvidedServiceRepo providedServiceRepository;

    @Autowired
    public ServiceManagementService(ProvidedServiceRepo providedServiceRepository) {
        this.providedServiceRepository = providedServiceRepository;
    }

    public ProvidedService saveService(ProvidedService providedService) {
        return providedServiceRepository.save(providedService);
    }


    public List<ProvidedService> findServicesByCompany(Integer companyId) {
        return providedServiceRepository.findByCompanyId(companyId);
    }

    public void deleteService(Integer id) {
        providedServiceRepository.deleteById(id);
    }

    public List<ProvidedService> findBySubstringInTitle(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            return List.of();
        }
        return providedServiceRepository.findByTitleContainingIgnoreCase(substring);
    }
    public List<ProvidedService> findBySubstringInDescription(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            return List.of();
        }
        return providedServiceRepository.findByDescriptionContainingIgnoreCase(substring);
    }
    public Optional<ProvidedService> findById(Integer id) {
        return providedServiceRepository.findById(id);
    }
}
