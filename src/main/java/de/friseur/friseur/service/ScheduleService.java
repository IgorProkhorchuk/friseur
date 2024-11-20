package de.friseur.friseur.service;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private final ScheduleRepository scheduleRepository;
    private Schedule schedule;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
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


}
