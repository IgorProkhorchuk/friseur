package de.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Handles user registration and manual authentication checks.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks whether a username is already taken.
     *
     * @param username desired username
     * @return true if another user exists with the same username
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    /**
     * Validates and persists a new user account with a default ROLE_USER assignment.
     *
     * @param username desired username
     * @param email    contact email
     * @param phone    contact phone number
     * @param password raw password
     * @param confirmPassword second password entry for validation
     * @return true when registration succeeds
     */
    public boolean registerUser(String username, String email, String phone, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            logger.warn("User registration failed: passwords do not match for username {}", username);
            throw new IllegalArgumentException("Passwords do not match!");
        }

        if (existsByUsername(username)) {
            logger.warn("User registration failed: username {} already exists", username);
            throw new IllegalArgumentException("Username already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of("ROLE_USER"));

        userRepository.save(user);
        logger.info("User registered successfully: username {}", username);
        return true;
    }

    /**
     * Performs a lightweight login check outside of Spring Security's standard flow.
     *
     * @param username user name
     * @param password raw password to verify
     * @return true when the provided combination matches a stored user
     */
    public boolean loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("User {} logged in successfully", username);
            return true;
        } else {
            logger.warn("Failed login attempt for user {}", username);
            return false;
        }
    }
}
