package de.friseur.friseur.repository;

import de.friseur.friseur.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentStatus(String appointmentStatus);

    List<Appointment> findBySlotDateAndSlotStartTimeAndAppointmentStatus(LocalDate date, LocalTime startTime, String appointmentStatus);
}

