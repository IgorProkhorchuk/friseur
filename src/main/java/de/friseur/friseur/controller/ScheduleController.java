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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Provides admin-facing endpoints for creating schedules, managing slots, and
 * viewing booking summaries.
 */
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

    /**
     * Entry point for the admin dashboard.
     *
     * @return admin landing template
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    /**
     * Displays a simple success confirmation page for bulk actions.
     *
     * @return success view
     */
    @GetMapping("/success")
    public String success() {
        return "success";
    }

    /**
     * Stores a minimal schedule window configured through HTMX or full page POST.
     *
     * @param schedule schedule payload with date bounds
     * @param model model for error/success flash
     * @param hxRequest HX-Request header if present
     * @return HTMX fragment or admin view depending on the caller
     */
    @PostMapping("/admin")
    public String setSchedule(@ModelAttribute Schedule schedule,
                              Model model,
                              @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
        logger.info("Received startDate: {}", schedule.getStartDate());
        logger.info("Received endDate: {}", schedule.getEndDate());

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            model.addAttribute("errorMessage", "Start and End Dates are required");
            return isHx(hxRequest) ? "admin :: set-schedule" : "admin";
        }

        scheduleRepository.save(schedule);
        model.addAttribute("successMessage", "Dates added successfully");
        return isHx(hxRequest) ? "admin :: set-schedule" : "admin";
    }

    /**
     * Builds the calendar grid for selecting timeslots in the UI.
     *
     * @param model template model with date range and timeslots
     * @return schedule creation view
     */
    @GetMapping("/admin/schedule")
    public String editWorkingHours(Model model) {
        List<LocalDateTime> dateRange = scheduleService.createDateRange(scheduleService.getLatestSchedule());
        List<LocalDateTime> timeslots = scheduleService.createTimeslots(scheduleService.createDateRange(scheduleService.getLatestSchedule()));
        model.addAttribute("dateRange", dateRange);
        model.addAttribute("timeslots", timeslots);
        return "create-schedule";
    }
    /**
     * Persists all chosen time slots and surfaces validation errors via flash attributes.
     *
     * @param selectedTimeslots ISO timestamp strings chosen in the UI
     * @param redirectAttributes flash scope for success/error messages
     * @return redirect to success or back to selector in case of validation issues
     */
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

    /**
     * Serves the admin dashboard view containing navigation cards.
     *
     * @return dashboard template name
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    /**
     * Lists all reserved slots so admins can monitor bookings.
     *
     * @param model template model with booked slot data
     * @return booked slots template
     */
    @GetMapping("/admin/booked")
    public String viewBookedSlots(Model model) {
        var bookedSlots = slotRepository.findAll()
                .stream()
                .filter(slot -> slot.getSlotStatus().toString().equals("RESERVED"))
                .toList();
        model.addAttribute("bookedSlots", bookedSlots);
        return "admin-booked";
    }

    /**
     * Shows every slot irrespective of status for auditing.
     *
     * @param model template model with all slot records
     * @return slot overview template
     */
    @GetMapping("/admin/schedule/all")
    public String viewAllSlots(Model model) {
        var allSlots = slotRepository.findAll();
        model.addAttribute("slots", allSlots);
        return "admin-schedule-all";
    }

    /**
     * Displays an interactive table for hiding/showing slots.
     *
     * @param model data model containing ordered slots and date filters
     * @return manage slots template
     */
    @GetMapping("/admin/slots/manage")
    public String manageSlots(Model model) {
        var slots = slotRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Slot::getTimeSlot))
                .toList();

        var slotDates = slots.stream()
                .map(slot -> slot.getTimeSlot().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("slots", slots);
        model.addAttribute("slotDates", slotDates);
        return "admin-manage-slots";
    }


    /**
     * Toggles an individual slot between {@link SlotStatus#AVAILABLE} and {@link SlotStatus#HIDDEN}.
     *
     * @param id slot identifier
     * @param redirectAttributes flash attributes capturing errors
     * @return redirect back to the management view
     */
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

    /**
     * Utility to detect HTMX requests so the corresponding partial view can be returned.
     *
     * @param hxHeader incoming HX header
     * @return true when the request should receive a fragment response
     */
    private boolean isHx(String hxHeader) {
        return hxHeader != null && hxHeader.equalsIgnoreCase("true");
    }
}
