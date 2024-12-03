package de.friseur.friseur.controller;

import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/")
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


}
