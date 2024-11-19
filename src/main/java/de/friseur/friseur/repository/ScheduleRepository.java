package de.friseur.friseur.repository;

import de.friseur.friseur.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStartDate(String startDate);
    List<Schedule> findByEndDate(String endDate);
    List<Schedule> findByStartDateAndEndDate(String startDate, String endDate);


}
