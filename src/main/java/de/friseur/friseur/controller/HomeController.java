package de.friseur.friseur.controller;


import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class HomeController {
    UserRepository repository;

    @GetMapping("/")
    public String hello() {
        return "helloWorld";
    }
}
