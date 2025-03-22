package de.friseur.friseur.controller;

import de.friseur.friseur.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        try {
            userService.registerUser(username, email, phone, password, confirmPassword);
            logger.info("User registration successful for username {}", username);
            return "redirect:/register?success";
        } catch (IllegalArgumentException e) {
            logger.error("User registration failed for username {}: {}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        logger.info("Login form displayed");
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("User logged out");
        return "logout";
    }
}
