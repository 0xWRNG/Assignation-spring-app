package com.example.auth_spring;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.ManagerToCompany;
import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.ManagerToCompanyRepo;
import com.example.auth_spring.repository.CompanyRepo;
import com.example.auth_spring.repository.UserRepo;
import com.example.auth_spring.service.ManagerToCompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagerToCompanyServiceTest {

    @Mock
    private ManagerToCompanyRepo managerToCompanyRepo;

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private ManagerToCompanyService managerToCompanyService;

    private User manager;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new User();
        manager.setId(1);
        manager.setLogin("manager1");

        company = new Company();
        company.setId(1);
        company.setName("Test Company");
    }

    @Test
    void testAddManagerToCompany() {
        managerToCompanyService.addManagerToCompany(manager, company);
        verify(managerToCompanyRepo, times(1)).save(any(ManagerToCompany.class));
    }

    @Test
    void testRemoveManagerFromCompany() {
        ManagerToCompany association = new ManagerToCompany(manager, company);
        when(managerToCompanyRepo.findByCompany(company)).thenReturn(List.of(association));
        when(companyRepo.findById(company.getId())).thenReturn(Optional.of(company));

        managerToCompanyService.removeManagerFromCompany(manager, company);

        verify(managerToCompanyRepo, times(1)).deleteAssociation(manager.getId(), company.getId());
    }

    @Test
    void testGetManagersByCompany() {
        when(managerToCompanyRepo.findByCompany(company)).thenReturn(List.of(new ManagerToCompany(manager, company)));

        var managers = managerToCompanyService.getManagersByCompany(company);
        assertEquals(1, managers.size());
        assertEquals(manager, managers.get(0).getManager());
    }

    @Test
    void testIsManagerInCompany() {
        ManagerToCompany association = new ManagerToCompany(manager, company);
        when(managerToCompanyRepo.findByManager(manager)).thenReturn(List.of(association));

        boolean result = managerToCompanyService.isManagerInCompany(manager, company);
        assertTrue(result);
    }

    @Test
    void testGetCompanyWithRelatedManagers() {
        when(managerToCompanyRepo.findByManager(manager)).thenReturn(List.of(new ManagerToCompany(manager, company)));

        var result = managerToCompanyService.getCompanyWithRelatedManagers(manager);
        assertEquals(1, result.size());
        assertEquals(company, result.get(0).first);
    }
}
