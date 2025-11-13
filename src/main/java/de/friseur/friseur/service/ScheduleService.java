package de.friseur.friseur.service;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.AppointmentRepository;
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

@Service
public class ScheduleService {

    private final AppointmentRepository appointmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final SlotRepository slotRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, SlotRepository slotRepository, AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Schedule getLatestSchedule() {
        Schedule latestSchedule = scheduleRepository.findTopByOrderByIdDesc();
        logger.info("Getting latest schedule");
        return latestSchedule;
    }

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

    public boolean saveSchedule(Schedule schedule) {
        List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(
                schedule.getStartDate(), schedule.getEndDate());
                if (overlappingSchedules.isEmpty()) {
            scheduleRepository.save(schedule);
            return true;
        } else {
            logger.warn("Cannot save schedule. Overlapping schedules found: {}", overlappingSchedules);
            return false;
    }
}
}

