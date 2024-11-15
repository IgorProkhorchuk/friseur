package de.friseur.friseur.service;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public List<Slot> getAvailableSlots(LocalDate date) {
        return slotRepository.findByDateAndSlotStatus(date, "available");
    }

    public List<String> getNextFiveDays(LocalDate today) {
        List<String> nextFiveDays = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            LocalDate nextDay = today.plusDays(i);
            nextFiveDays.add(nextDay.toString());
        }
        return nextFiveDays;
    }
}
