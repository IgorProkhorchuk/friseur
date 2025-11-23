package de.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

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

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean registerUser(String username, String email, String phone, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            logger.warn("User registration failed: passwords do not match for username {}", username);
            throw new IllegalArgumentException("Passwords do not match!");
        }

        if (existsByEmail(email)) {
            logger.warn("User registration failed: email {} already exists", email);
            throw new IllegalArgumentException("User with this email already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of("ROLE_USER"));

        userRepository.save(user);
        logger.info("User registered successfully: username {}: email: {}", username, email);
        return true;
    }

    public boolean loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("User {} logged in successfully", email);
            return true;
        } else {
            logger.warn("Failed login attempt for user {}", email);
            return false;
        }
    }
}
