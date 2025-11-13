package de.friseur.friseur.service;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.repository.SlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Builds date ranges and slots from admin-defined schedules.
 */
@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final SlotRepository slotRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, SlotRepository slotRepository) {
        this.scheduleRepository = scheduleRepository;
        this.slotRepository = slotRepository;
    }

    /**
     * Retrieves the newest persisted schedule window, which acts as the active configuration.
     *
     * @return the most recent schedule or null if none exist
     */
    public Schedule getLatestSchedule() {
        Schedule latestSchedule = scheduleRepository.findTopByOrderByIdDesc();
        logger.info("Getting latest schedule");
        return latestSchedule;
    }

    /**
     * Expands the inclusive start/end dates of a schedule into a list of day boundaries.
     *
     * @param latestSchedule active schedule record
     * @return list of day entries covering the configured period
     */
    public List<LocalDateTime> createDateRange(Schedule latestSchedule) {
        List<LocalDateTime> dateRange = new ArrayList<>();
        LocalDate startDate = latestSchedule.getStartDate();
        LocalDate endDate = latestSchedule.getEndDate();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        while (!startDateTime.isAfter(endDateTime)) {
            dateRange.add(startDateTime);
            startDateTime = startDateTime.plusDays(1);
        }
        return dateRange;
    }

    /**
     * Generates hourly time slots (9-21) for each day in the provided range.
     *
     * @param dateRange list of days to expand
     * @return full list of discrete slot timestamps
     */
    public List<LocalDateTime> createTimeslots(List<LocalDateTime> dateRange) {
        List<LocalDateTime> timeslots = new ArrayList<>();
        for (LocalDateTime date : dateRange) {
            for (int hour = 9; hour < 21; hour++) {
                LocalDateTime time = date.withHour(hour).withMinute(0);
                timeslots.add(time);
            }
        }
        return timeslots;
    }

    /**
     * Converts the selected slot timestamps into {@link Slot} entities and persists them as available.
     *
     * @param selectedTimeslots list of ISO date-time strings selected in the UI
     */
    public void saveSelectedTimeslots(List<String> selectedTimeslots) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Create and save new slots
        List<Slot> newSlots = selectedTimeslots.stream()
                .map(timeSlotStr -> {
                    Slot slot = new Slot();
                    slot.setTimeSlot(LocalDateTime.parse(timeSlotStr, formatter));
                    slot.setSlotStatus(SlotStatus.AVAILABLE);
                    slot.setAppointment(null);
                    return slot;
                })
                .collect(Collectors.toList());

        slotRepository.saveAll(newSlots);
    }
}
