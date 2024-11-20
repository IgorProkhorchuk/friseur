package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStartDate(LocalDate startDate);
    List<Schedule> findByEndDate(LocalDate endDate);
    List<Schedule> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);


    Schedule findTopByOrderByIdDesc();
}
