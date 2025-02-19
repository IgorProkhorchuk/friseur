package de.friseur.friseur.controller;

import de.friseur.friseur.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;
import java.util.logging.Logger;

@Controller
public class RegAuthController {

private static final Logger logger = Logger.getLogger(RegAuthController.class.getName());

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam("phone") String phone,
                               Model model) {
        String result = userService.registerUser(username, password, phone);
        if (result.equals("Username already exists")) {
            model.addAttribute("errorMessage", result);
            logger.info("Username already exists" + username);
            return result;
        }
        model.addAttribute("successMessage", "User registered successfully");
        logger.info("User registered successfully" + username);
        return "redirect:/slots";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            Model model, HttpSession session) {
        logger.info("Attempting to log in user: " + username);
        String result = userService.authenticateAndLoginUser(username, password, session);
        if (result.equals("Invalid username or password")) {
            model.addAttribute("errorMessage", result);
            logger.info("Login failed for user: " + username);
            return "login"; // Ensure the login view is returned on failure
        }
        logger.info("User logged in successfully: " + username);
        return "redirect:/slots";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }
}
