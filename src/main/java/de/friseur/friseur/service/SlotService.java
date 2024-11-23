package de.friseur.friseur.service;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.repository.SlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {
    private static final Logger logger = LoggerFactory.getLogger(SlotService.class);
    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

//    public List<Slot> getAvailableSlots(LocalDateTime timeslot) {
//        return slotRepository.findByDateAndSlotStatus(timeslot, "available");
//    }

//    public List<String> getNextFiveDays(LocalDate today) {
//        List<String> nextFiveDays = new ArrayList<>();
//
//        for (int i = 1; i <= 5; i++) {
//            LocalDate nextDay = today.plusDays(i);
//            nextFiveDays.add(nextDay.toString());
//        }
//        return nextFiveDays;
//    }
}
