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

/**
 * Handles registration, login, and logout views for site users.
 */
@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Renders the registration page.
     *
     * @return the register template name
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    /**
     * Persists a new user and surfaces validation errors back to the form.
     *
     * @param username         desired username
     * @param email            user email
     * @param phone            contact phone
     * @param password         chosen password
     * @param confirmPassword  confirmation password
     * @param model            model for error feedback
     * @return redirect when successful or the form on validation failure
     */
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

    /**
     * Renders the login page.
     *
     * @return the login template name
     */
    @GetMapping("/login")
    public String showLoginForm() {
        logger.info("Login form displayed");
        return "login";
    }

    /**
     * Performs a manual credential check. On success the user is redirected to
     * the slot overview, otherwise the login page is re-rendered with errors.
     *
     * @param username supplied user name
     * @param password supplied password
     * @param model    model to attach error state
     * @return redirect target or login view
     */
    @PostMapping("/login")
    public String loginUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model
    ) {
        if (userService.loginUser(username, password)) {
            logger.info("User {} logged in successfully", username);
            return "redirect:/slots";
        } else {
            logger.warn("Failed login attempt for user {}", username);
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    /**
     * Simple logout endpoint used as a landing page after Spring Security logs
     * the user out.
     *
     * @return view name confirming logout
     */
    @GetMapping("/logout")
    public String logout() {
        logger.info("User logged out");
        return "logout";
    }
}
