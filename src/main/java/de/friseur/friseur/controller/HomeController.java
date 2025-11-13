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

/**
 * Handles public facing pages (home, shop) and slot discovery/booking workflows.
 */
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

    /**
     * Landing page for anonymous visitors.
     *
     * @return template name for the marketing home page
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Internal home route for authenticated users that points to {@code index.html}.
     *
     * @return template name for the application index view
     */
    @GetMapping("/home")
    public String hello() {
        return "index";
    }

    /**
     * Displays the shop page.
     *
     * @return shop template name
     */
    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    /**
     * Shows all future slots grouped by date and time.
     *
     * @param model model enriched with the available date list and slot list
     * @return the slots template
     */
    @GetMapping("/slots")
    public String getAllAvailableSlots(Model model) {
        logger.info("Getting all available slots");
        List<LocalDate> availableDates = slotService.getAllAvailableDates();
        List<LocalDateTime> availableSlots = slotService.getAllAvailableSlots();
        model.addAttribute("dates", availableDates);  // Pass LocalDate list
        model.addAttribute("slots", availableSlots);  // Pass LocalDateTime list
        return "slots";
    }

    /**
     * Attempts to reserve the requested slot for the current user and returns a fragment
     * that can display either a confirmation or an error message.
     *
     * @param timeSlot slot timestamp selected by the user
     * @param model    holder for the success/error message
     * @return confirmation fragment template
     */
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
