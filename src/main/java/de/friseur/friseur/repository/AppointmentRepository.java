package de.friseur.friseur.repository;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import de.friseur.friseur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    public List<Appointment> findBySlotTimeSlotAndAppointmentStatus(
            LocalDateTime timeSlot,
            AppointmentStatus appointmentStatus
    );

    // This method is used by AppointmentServiceImpl to get upcoming appointments for a user
    List<Appointment> findByUser_UsernameAndAppointmentStatusAndSlot_TimeSlotAfterOrderBySlot_TimeSlotAsc(
            String username,
            AppointmentStatus status,
            LocalDateTime dateTime
    );

    List<Appointment> findByUser(User user);

}
