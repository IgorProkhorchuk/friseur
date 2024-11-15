package de.friseur.friseur.repository;

import de.friseur.friseur.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Integer> {
    List<Slot> findByDateAndSlotStatus(LocalDate date, String slotStatus);

}
