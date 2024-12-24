package com.example.auth_spring.service;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.ManagerToCompany;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.CompanyRepo;
import com.example.auth_spring.repository.ManagerToCompanyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepo companyRepository;
    private final ManagerToCompanyRepo managerToCompanyRepo;

    @Autowired
    public CompanyService(CompanyRepo companyRepo, ManagerToCompanyRepo managerToCompanyRepo) {
        this.companyRepository = companyRepo;
        this.managerToCompanyRepo = managerToCompanyRepo;
    }


    public Optional<Company> findById(Integer id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompaniesByUser(User user) {
        return companyRepository.findByCreatedBy(user);
    }

    public List<Company> findManagedCompanyByUserId(User user) {
        List<ManagerToCompany> managerToCompanyList =  managerToCompanyRepo.findByManager(user);
        return managerToCompanyList.stream()
                .map(ManagerToCompany::getCompany)
                .collect(Collectors.toList());
    }
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }
    public Company updateCompany(Company company) {
        return companyRepository.save(company);
    }
    public void deleteCompany(Integer id) {
        companyRepository.deleteById(id);
    }
    public List<Company> findBySubstring(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            return List.of();
        }
        Set<Company> companies = new HashSet<>();
        companies.addAll(companyRepository.findByNameContainingIgnoreCase(substring));
        companies.addAll(companyRepository.findByDescriptionContainingIgnoreCase(substring));
        return new ArrayList<>(companies);
    }

    public void addManager(User user, Company company) {
        managerToCompanyRepo.save( new ManagerToCompany(user, company));
    }


}
