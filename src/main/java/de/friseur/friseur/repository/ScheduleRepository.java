package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findTopByOrderByIdDesc();
}
