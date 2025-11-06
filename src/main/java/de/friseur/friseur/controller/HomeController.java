package de.friseur.friseur.controller;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.AppointmentService;
import de.friseur.friseur.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
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
    @Autowired
    UserRepository repository;
    @Autowired
    private SlotService slotService;
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home")
    public String hello() {
        return "index";
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
public String reserveSlot(@RequestParam("slot") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeSlot,  Model model) {

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    int userID = repository.findByUsername(username).orElseThrow().getUserId();

    boolean savedSlot = slotService.reserveSlot(timeSlot, userID, username, "Haircut");
    if (savedSlot) {
        model.addAttribute("message", "Slot reserved successfully");
    } else {
        model.addAttribute("message", "Slot reservation failed");
    }
    return "fragments/confirmation";
}

}
