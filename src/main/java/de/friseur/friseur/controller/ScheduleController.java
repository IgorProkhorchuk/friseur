package de.friseur.friseur.controller;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public String setSchedule(@ModelAttribute Schedule schedule, Model model) {
        logger.info("Received startDate: {}", schedule.getStartDate());
        logger.info("Received endDate: {}", schedule.getEndDate());

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            model.addAttribute("errorMessage", "Start and End Dates are required");
            return "admin";
        }

        scheduleRepository.save(schedule);
        model.addAttribute("successMessage", "Dates added successfully");
        return "admin";
    }

    @GetMapping("/admin/schedule")
    public String editWorkingHours(Model model) {
        List<LocalDateTime> dateRange = scheduleService.createDateRange(scheduleService.getLatestSchedule());
        List<LocalDateTime> timeslots = scheduleService.createTimeslots(scheduleService.createDateRange(scheduleService.getLatestSchedule()));
        model.addAttribute("dateRange", dateRange);
        model.addAttribute("timeslots", timeslots);
        return "schedule";
    }

}
