package de.friseur.friseur.service;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.SlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SlotService {
    private static final Logger logger = LoggerFactory.getLogger(SlotService.class);
    private final SlotRepository slotRepository;


    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public List<LocalDateTime> getAllAvailableSlots() {
        try {
            logger.info("Getting all available slots");
            LocalDateTime today = LocalDateTime.now();
            List<Slot> availableSlots = slotRepository.findAllAvailableSlots(today, SlotStatus.AVAILABLE);

            return availableSlots.stream()
                    .map(Slot::getTimeSlot)  // Get the time slot
                    .sorted()  // Sort in natural (chronological) order
                    .collect(Collectors.toList());  // Collect as a list of LocalDateTime
        } catch (Exception e) {
            logger.error("Error while getting available slots", e);
            return Collections.emptyList();
        }
    }

    public List<LocalDate> getAllAvailableDates() {
        try {
            logger.info("Getting all available dates");
            LocalDateTime now = LocalDateTime.now();
            List<Slot> availableSlots = slotRepository.findAllAvailableSlots(now, SlotStatus.AVAILABLE);

            return availableSlots.stream()
                    .map(Slot::getTimeSlot)  // Get the time slot
                    .map(LocalDateTime::toLocalDate)  // Convert to LocalDate
                    .distinct()  // Remove duplicates
                    .sorted()  // Sort in natural (chronological) order
                    .toList();  // Collect as a list of LocalDate
        } catch (NullPointerException e) {
            logger.error("NullPointerException caught while getting available dates", e);
            return List.of();
        }
    }




}
