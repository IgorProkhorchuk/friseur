package de.friseur.friseur.controller;

import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.AppointmentService;
import de.friseur.friseur.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final UserRepository repository;
    private final SlotService slotService;
    private final AppointmentService appointmentService;

    public HomeController(UserRepository repository, SlotService slotService, AppointmentService appointmentService) {
        this.repository = repository;
        this.slotService = slotService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        displayUserName(model, authentication);
        return "index";
    }

    @GetMapping("/home")
    public String hello(Model model, Authentication authentication) {
        displayUserName(model, authentication);
        return "index";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    @GetMapping("/slots")
    public String getAllAvailableSlots(Model model) {
        logger.info("Getting all available slots");
        List<LocalDate> availableDates = slotService.getAllAvailableDates();
        List<LocalDateTime> availableSlots = slotService.getAllAvailableSlots();
        model.addAttribute("dates", availableDates);  // Pass LocalDate list
        model.addAttribute("slots", availableSlots);  // Pass LocalDateTime list
        return "slots";
    }

    @PostMapping("/slots")
    public String reserveSlot(@RequestParam("slot") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeSlot, Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = repository.findByEmail(email).orElseThrow();
        int userID = user.getUserId();

        boolean savedSlot = slotService.reserveSlot(timeSlot, userID, user.getDisplayName(), "Haircut");
        if (savedSlot) {
            model.addAttribute("message", "Slot reserved successfully");
        } else {
            model.addAttribute("message", "Slot reservation failed");
        }
        return "fragments/confirmation";
    }

    private void displayUserName(Model model, Authentication authentication) {
        if (authentication == null) {
            return;
        }

        boolean isStandardUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_USER"::equals);

        if (isStandardUser) {
            repository.findByEmail(authentication.getName())
                    .ifPresent(user -> model.addAttribute("userName", user.getDisplayName()));
        }
    }

}
