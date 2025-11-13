package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for persisting {@link Schedule} windows.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    /**
     * Grabs the most recently created schedule entry, used as the active planning window.
     */
    Schedule findTopByOrderByIdDesc();
}
