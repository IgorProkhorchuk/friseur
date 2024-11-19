package de.friseur.friseur.controller;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleService scheduleService;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @PostMapping("/admin")
    public String setSchedule(@ModelAttribute Schedule schedule) {
        logger.info("Received startDate: {}", schedule.getStartDate());
        logger.info("Received endDate: {}", schedule.getEndDate());

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            return "redirect:/admin?error=missingDates";
        }

        scheduleRepository.save(schedule);
        return "admin :: schedule";
    }

}
