package de.friseur.friseur.repository;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface SlotRepository extends JpaRepository<Slot, Integer> {

    @Query("SELECT s FROM Slot s WHERE s.timeSlot >= :startDateTime AND s.slotStatus = :status")
    List<Slot> findAllAvailableSlots(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("status") SlotStatus status
    );

    List<Slot> findAllByTimeSlotGreaterThanEqualAndSlotStatus(LocalDateTime startTime, SlotStatus status);
}
