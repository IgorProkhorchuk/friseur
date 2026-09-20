package de.friseur.friseur.controller;

import de.friseur.friseur.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword
    ) {
        try {
            userService.registerUser(username, email, phone, password, confirmPassword);
            return "redirect:/login?success=true";
        } catch (IllegalArgumentException e) {
            String redirectUrl = UriComponentsBuilder
                    .fromPath("/register")
                    .queryParam("error", e.getMessage())
                    .build()
                    .encode()
                    .toUriString();
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            logger.error("Unexpected registration error for user {}", email, e);
            String redirectUrl = UriComponentsBuilder
                    .fromPath("/register")
                    .queryParam("error", "Registration failed. Please try again.")
                    .build()
                    .encode()
                    .toUriString();
            return "redirect:" + redirectUrl;
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            return isAdmin ? "redirect:/admin/dashboard" : "redirect:/home";
        }

        logger.info("Login form displayed");
        return "login";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
