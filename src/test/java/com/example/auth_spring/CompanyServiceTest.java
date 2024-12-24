package com.example.auth_spring;


import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.CompanyRepo;
import com.example.auth_spring.repository.ManagerToCompanyRepo;
import com.example.auth_spring.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceTest {

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private ManagerToCompanyRepo managerToCompanyRepo;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1);
        user.setLogin("testUser");

        company = new Company();
        company.setId(1);
        company.setName("Test Company");
        company.setCreatedBy(user);
    }

    @Test
    void testSaveCompany() {
        when(companyRepo.save(any(Company.class))).thenReturn(company);

        Company savedCompany = companyService.saveCompany(company);
        assertNotNull(savedCompany);
        assertEquals("Test Company", savedCompany.getName());
        verify(companyRepo, times(1)).save(company);
    }

    @Test
    void testFindById() {
        when(companyRepo.findById(1)).thenReturn(Optional.of(company));

        Optional<Company> foundCompany = companyService.findById(1);
        assertTrue(foundCompany.isPresent());
        assertEquals("Test Company", foundCompany.get().getName());
    }

    @Test
    void testDeleteCompany() {
        doNothing().when(companyRepo).deleteById(1);

        companyService.deleteCompany(1);
        verify(companyRepo, times(1)).deleteById(1);
    }

    @Test
    void testGetCompaniesByUser() {
        when(companyRepo.findByCreatedBy(user)).thenReturn(List.of(company));

        List<Company> companies = companyService.getCompaniesByUser(user);
        assertEquals(1, companies.size());
        assertEquals("Test Company", companies.get(0).getName());
    }

    @Test
    void testUpdateCompany() {
        when(companyRepo.save(any(Company.class))).thenReturn(company);

        Company updatedCompany = companyService.updateCompany(company);
        assertNotNull(updatedCompany);
        assertEquals("Test Company", updatedCompany.getName());
        verify(companyRepo, times(1)).save(company);
    }
}
