package de.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password, String phone) {
        if (userRepository.findByUserName(username) != null) {
            return "username already exists";
        }

        User user = new User();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserPhone(phone);
        userRepository.save(user);

        
        return  user.toString();
    }

    public String authenticateAndLoginUser(String username, String password, HttpSession session) {
        boolean isAuthenticated = authenticateUser(username, password);
        if (!isAuthenticated) {
            return "Invalid username or password";
        }
        session.setAttribute("username", username);
        return "success";
    }

    private boolean authenticateUser(String username, String password) {
        User user = userRepository.findByUserName(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }
}
