package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findTopByOrderByIdDesc();
}
