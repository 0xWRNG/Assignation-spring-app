package com.example.auth_spring;

import com.example.auth_spring.model.Company;
import com.example.auth_spring.model.ProvidedService;
import com.example.auth_spring.repository.ProvidedServiceRepo;
import com.example.auth_spring.service.ServiceManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceManagementServiceTest {

    @Mock
    private ProvidedServiceRepo providedServiceRepository;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveService() {
        ProvidedService service = new ProvidedService();
        service.setTitle("Test Service");
        service.setDescription("Test Description");
        service.setDuration(60);

        when(providedServiceRepository.save(service)).thenReturn(service);

        ProvidedService savedService = serviceManagementService.saveService(service);

        assertNotNull(savedService);
        assertEquals("Test Service", savedService.getTitle());
        verify(providedServiceRepository, times(1)).save(service);
    }

    @Test
    void testFindServicesByCompany() {
        Integer companyId = 1;
        List<ProvidedService> services = new ArrayList<>();
        ProvidedService service1 = new ProvidedService();
        service1.setCompany(new Company());
        service1.getCompany().setId(companyId);
        services.add(service1);

        when(providedServiceRepository.findByCompanyId(companyId)).thenReturn(services);

        List<ProvidedService> foundServices = serviceManagementService.findServicesByCompany(companyId);

        assertNotNull(foundServices);
        assertEquals(1, foundServices.size());
        assertEquals(companyId, foundServices.get(0).getCompany().getId());
        verify(providedServiceRepository, times(1)).findByCompanyId(companyId);
    }

    @Test
    void testDeleteService() {
        Integer serviceId = 1;
        doNothing().when(providedServiceRepository).deleteById(serviceId);

        serviceManagementService.deleteService(serviceId);

        verify(providedServiceRepository, times(1)).deleteById(serviceId);
    }

    @Test
    void testFindById() {
        Integer serviceId = 1;
        ProvidedService service = new ProvidedService();
        service.setId(serviceId);

        when(providedServiceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Optional<ProvidedService> foundService = serviceManagementService.findById(serviceId);

        assertTrue(foundService.isPresent());
        assertEquals(serviceId, foundService.get().getId());
        verify(providedServiceRepository, times(1)).findById(serviceId);
    }
}
