package de.friseur.friseur.controller;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleService scheduleService;

    @Autowired
    SlotRepository slotRepository;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
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
        return "create-schedule";
    }
    @PostMapping("/admin/save-schedule")
    public String saveSelectedTimeslots(@RequestParam(required = false) List<String> selectedTimeslots,
                                        RedirectAttributes redirectAttributes) {
        try {
            if (selectedTimeslots == null || selectedTimeslots.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one timeslot");
                return "redirect:/admin/schedule";
            }

            scheduleService.saveSelectedTimeslots(selectedTimeslots);
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Successfully added %d new timeslots", selectedTimeslots.size()));

            logger.info("Successfully saved {} new timeslots", selectedTimeslots.size());
        } catch (Exception e) {
            logger.error("Error saving timeslots", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving timeslots: " + e.getMessage());
        }

        return "redirect:/success";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/admin/booked")
    public String viewBookedSlots(Model model) {
        var bookedSlots = slotRepository.findAll()
                .stream()
                .filter(slot -> slot.getSlotStatus().toString().equals("RESERVED"))
                .toList();
        model.addAttribute("bookedSlots", bookedSlots);
        return "admin-booked";
    }

    @GetMapping("/admin/schedule/all")
    public String viewAllSlots(Model model) {
        var allSlots = slotRepository.findAll();
        model.addAttribute("slots", allSlots);
        return "admin-schedule-all";
    }

    @GetMapping("/admin/slots/manage")
    public String manageSlots(Model model) {
        var slots = slotRepository.findAll();

        // Group by LocalDate
        Map<LocalDate, List<Slot>> grouped = slots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getTimeSlot().toLocalDate(),
                        TreeMap::new, Collectors.toList()));

        model.addAttribute("groupedSlots", grouped);
        return "admin-manage-slots";
    }


    @PostMapping("/admin/slots/toggle/{id}")
    public String toggleSlot(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        var slot = slotRepository.findById(id).orElse(null);
        if (slot == null) {
            redirectAttributes.addFlashAttribute("error", "Slot not found");
            return "redirect:/admin/slots/manage";
        }

        if (slot.getSlotStatus() == SlotStatus.AVAILABLE) {
            slot.setSlotStatus(SlotStatus.HIDDEN);
        } else if (slot.getSlotStatus() == SlotStatus.HIDDEN) {
            slot.setSlotStatus(SlotStatus.AVAILABLE);
        }

        slotRepository.save(slot);
        return "redirect:/admin/slots/manage";
    }

}
