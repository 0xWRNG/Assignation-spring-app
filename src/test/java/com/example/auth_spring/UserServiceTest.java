package com.example.auth_spring;

import com.example.auth_spring.model.User;
import com.example.auth_spring.repository.UserRepo;
import com.example.auth_spring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setLogin("testUser");
        user.setPassword("password");

        when(userRepo.existsByLogin(user.getLogin())).thenReturn(false);
        userService.register(user);

        verify(userRepo, times(1)).save(user);
    }

    @Test
    void testRegisterUser_ExistingLogin() {
        User user = new User();
        user.setLogin("testUser");
        user.setPassword("password");

        when(userRepo.existsByLogin(user.getLogin())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(user);
        });

        assertEquals("Login already exists", exception.getMessage());
    }

    @Test
    void testGetUserByLogin() {
        User user = new User();
        user.setLogin("testUser");
        user.setPassword("password");

        when(userRepo.findByLogin(user.getLogin())).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByLogin("testUser");
        assertEquals(user.getLogin(), foundUser.getLogin());
    }

    @Test
    void testGetUserByLogin_NotFound() {
        when(userRepo.findByLogin("nonExistentUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByLogin("nonExistentUser");
        });

        assertEquals("Login not found", exception.getMessage());
    }
}
