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
    void existsByUsername_shouldReturnTrue_whenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));
        assertTrue(userService.existsByUsername("testuser"));
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUserDoesNotExist() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertFalse(userService.existsByUsername("testuser"));
    }

    @Test
    void registerUser_shouldRegisterUserSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
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
    void registerUser_shouldThrowException_whenUsernameExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "test@test.com", "123456789", "password", "password");
        });
    }

    @Test
    void loginUser_shouldReturnTrue_whenCredentialsAreCorrect() {
        User user = new User();
        user.setPassword("encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        assertTrue(userService.loginUser("testuser", "password"));
    }

    @Test
    void loginUser_shouldReturnFalse_whenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertFalse(userService.loginUser("testuser", "password"));
    }

    @Test
    void loginUser_shouldReturnFalse_whenPasswordIsIncorrect() {
        User user = new User();
        user.setPassword("encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        assertFalse(userService.loginUser("testuser", "password"));
    }
}
