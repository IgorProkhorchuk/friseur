package de.friseur.friseur.controller;


import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.SlotService;
import de.friseur.friseur.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping
public class HomeController {
    @Autowired
    UserRepository repository;
    @Autowired
    private SlotService slotService;

    @GetMapping("/")
    public String hello() {
        return "index";
    }
    @GetMapping("/book")
    public String book() {
        return "book :: bookingSlots";
    }

    @GetMapping("/slots")
    public String getSlots(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("nextFiveDays", slotService.getNextFiveDays(today));
        return "slots";
    }

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

}
