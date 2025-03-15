package de.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User registerUser(String username, String phone, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setUserPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Collections.singleton("ROLE_USER"));
        return userRepository.save(user);
    }

}
