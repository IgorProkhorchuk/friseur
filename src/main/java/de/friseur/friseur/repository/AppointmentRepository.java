package de.friseur.friseur.repository;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    public List<Appointment> findBySlotTimeSlotAndAppointmentStatus(
            LocalDateTime timeSlot,
            AppointmentStatus appointmentStatus
    );
}

