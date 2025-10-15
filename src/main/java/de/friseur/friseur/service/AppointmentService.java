package de.friseur.friseur.service;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.service.exception.AppointmentNotFoundException;
import de.friseur.friseur.service.exception.UnauthorizedCancelException;

import java.util.List;

public interface AppointmentService {

    /**
     * Retrieves all upcoming appointments for a given user.
     *
     * @param username The username of the user.
     * @return A list of upcoming appointments.
     */
    List<Appointment> getUpcomingAppointmentsForUser(String username);

    /**
     * Cancels an appointment for a user.
     * This method should verify that the appointment exists and belongs to the specified user
     * before proceeding with the cancellation.
     *
     * @param appointmentId The ID of the appointment to cancel.
     * @param username      The username of the user requesting the cancellation.
     * @throws AppointmentNotFoundException  if the appointment with the given ID does not exist.
     * @throws UnauthorizedCancelException if the user is not authorized to cancel this appointment.
     */
    void cancelUserAppointment(Long appointmentId, String username)
            throws AppointmentNotFoundException, UnauthorizedCancelException;

    // You can add other methods here as needed, e.g., for booking appointments,
    // finding available slots, etc.
    // Appointment bookAppointment(String username, LocalDateTime dateTime, String serviceName);
}
