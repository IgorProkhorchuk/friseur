package de.friseur.friseur.repository;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import de.friseur.friseur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data access layer for {@link Appointment} aggregates.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    /**
     * Finds all appointments occupying the specified slot while matching the desired status.
     */
    public List<Appointment> findBySlotTimeSlotAndAppointmentStatus(
            LocalDateTime timeSlot,
            AppointmentStatus appointmentStatus
    );

    /**
     * Retrieves a user's appointments that are still upcoming and chronologically after the provided time.
     */
    List<Appointment> findByUser_UsernameAndAppointmentStatusAndSlot_TimeSlotAfterOrderBySlot_TimeSlotAsc(
            String username,
            AppointmentStatus status,
            LocalDateTime dateTime
    );

    /**
     * Returns every appointment belonging to the supplied user.
     */
    List<Appointment> findByUser(User user);

}
