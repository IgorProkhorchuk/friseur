package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findTopByOrderByIdDesc();

    @Query("SELECT s FROM Schedule s WHERE s.startDate <= :endDate AND s.endDate >= :startDate")
    List<Schedule> findOverlappingSchedules(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
