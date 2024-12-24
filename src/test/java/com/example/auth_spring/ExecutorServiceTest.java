package com.example.auth_spring;


import com.example.auth_spring.model.Executor;
import com.example.auth_spring.repository.ExecutorRepo;
import com.example.auth_spring.service.ExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutorServiceTest {

    @Mock
    private ExecutorRepo executorRepository;

    @InjectMocks
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveExecutor() {
        Executor executor = new Executor();
        executor.setName("John");
        executor.setSurname("Doe");

        when(executorRepository.save(executor)).thenReturn(executor);

        Executor savedExecutor = executorService.saveExecutor(executor);

        assertNotNull(savedExecutor);
        assertEquals("John", savedExecutor.getName());
        verify(executorRepository, times(1)).save(executor);
    }

    @Test
    void testFindById() {
        Executor executor = new Executor();
        executor.setId(1);
        executor.setName("John");
        executor.setSurname("Doe");

        when(executorRepository.findById(1)).thenReturn(Optional.of(executor));

        Optional<Executor> foundExecutor = executorService.findById(1);

        assertTrue(foundExecutor.isPresent());
        assertEquals("John", foundExecutor.get().getName());
        verify(executorRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        when(executorRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Executor> foundExecutor = executorService.findById(1);

        assertFalse(foundExecutor.isPresent());
        verify(executorRepository, times(1)).findById(1);
    }

    @Test
    void testDeleteExecutor() {
        int executorId = 1;

        executorService.deleteExecutor(executorId);

        verify(executorRepository, times(1)).deleteById(executorId);
    }

    @Test
    void testFindBySubstring() {
        String substring = "John";
        Executor executor = new Executor();
        executor.setName("John");

        when(executorRepository.findBySubstringInAttributes(substring)).thenReturn(List.of(executor));

        List<Executor> foundExecutors = executorService.findBySubstring(substring);

        assertEquals(1, foundExecutors.size());
        assertEquals("John", foundExecutors.get(0).getName());
        verify(executorRepository, times(1)).findBySubstringInAttributes(substring);
    }
}
