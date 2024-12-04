package de.friseur.friseur.repository;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SlotRepository extends JpaRepository<Slot, Integer> {

    @Query("SELECT s FROM Slot s WHERE s.timeSlot >= :startDateTime AND s.slotStatus = :status")
    List<Slot> findAllAvailableSlots(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("status") SlotStatus status
    );

    List<Slot> findAllByTimeSlotGreaterThanEqualAndSlotStatus(LocalDateTime startTime, SlotStatus status);

    Optional<Slot> findByTimeSlotAndSlotStatus(LocalDateTime timeSlot, SlotStatus status);

    Slot findByTimeSlot(LocalDateTime timeSlot);
}
