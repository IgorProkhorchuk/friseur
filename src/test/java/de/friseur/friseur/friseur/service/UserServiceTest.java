package de.friseur.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void existsByEmail_shouldReturnTrue_whenUserExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        assertTrue(userService.existsByEmail("test@test.com"));
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserDoesNotExist() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        assertFalse(userService.existsByEmail("test@test.com"));
    }

    @Test
    void registerUser_shouldRegisterUserSuccessfully() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        boolean result = userService.registerUser("testuser", "test@test.com", "123456789", "password", "password");

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenPasswordsDoNotMatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "test@test.com", "123456789", "password", "wrongpassword");
        });
    }

    @Test
    void registerUser_shouldThrowException_whenEmailExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "test@test.com", "123456789", "password", "password");
        });
    }


}
