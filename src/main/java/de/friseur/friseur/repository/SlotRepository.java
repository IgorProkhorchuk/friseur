package de.friseur.friseur.repository;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CRUD operations on {@link Slot} entities.
 */
public interface SlotRepository extends JpaRepository<Slot, Integer> {

    /**
     * Finds slots that occur after the requested start date and currently match the provided status.
     *
     * @param startDateTime lower bound for slot time
     * @param status        status to filter on (e.g. AVAILABLE)
     * @return list of matching slots sorted by natural order
     */
    @Query("SELECT s FROM Slot s WHERE s.timeSlot >= :startDateTime AND s.slotStatus = :status")
    List<Slot> findAllAvailableSlots(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("status") SlotStatus status
    );

    /**
     * Retrieves all slots that start at or after a given timestamp and have the provided status.
     */
    List<Slot> findAllByTimeSlotGreaterThanEqualAndSlotStatus(LocalDateTime startTime, SlotStatus status);

    /**
     * Looks up a slot by its exact timestamp and status combination.
     */
    Optional<Slot> findByTimeSlotAndSlotStatus(LocalDateTime timeSlot, SlotStatus status);

    /**
     * Fetches a slot by its timestamp regardless of status.
     */
    Slot findByTimeSlot(LocalDateTime timeSlot);
}
