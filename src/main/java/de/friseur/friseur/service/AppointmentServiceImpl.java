package de.friseur.friseur.service;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.AppointmentRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.exception.AppointmentNotFoundException;
import de.friseur.friseur.service.exception.UnauthorizedCancelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private SlotRepository slotRepository;

    @Override
    public List<Appointment> getUpcomingAppointmentsForUser(String username) {
        log.info("Fetching upcoming appointments for user: {}", username);
        return appointmentRepository.findByUser_UsernameAndAppointmentStatusAndSlot_TimeSlotAfterOrderBySlot_TimeSlotAsc(
                username, AppointmentStatus.UPCOMING, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cancelUserAppointment(Long appointmentId, String username)
            throws AppointmentNotFoundException, UnauthorizedCancelException {
        log.info("Attempting to cancel appointment ID: {} for user: {}", appointmentId, username);

        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);

        if (appointmentOptional.isEmpty()) {
            log.warn("Appointment not found: {}", appointmentId);
            throw new AppointmentNotFoundException("Appointment with ID " + appointmentId + " not found.");
        }

        Appointment appointment = appointmentOptional.get();

        if (appointment.getUser() == null || !Objects.equals(appointment.getUser().getUsername(), username)) {
            log.warn("User {} unauthorized to cancel appointment {} owned by {}",
                    username, appointmentId, (appointment.getUser() != null ? appointment.getUser().getUsername() : "unknown"));
            throw new UnauthorizedCancelException("You are not authorized to cancel this appointment.");
        }

        if (appointment.getAppointmentStatus() != AppointmentStatus.UPCOMING) {
            log.warn("Attempt to cancel appointment {} which is not UPCOMING. Status: {}", appointmentId, appointment.getAppointmentStatus());
            throw new IllegalStateException("Only upcoming appointments can be cancelled.");
        }

        // Business rule: Cannot cancel if appointment's slot time is in the past or too soon (e.g., within 24 hours)
        if (appointment.getSlot() != null && appointment.getSlot().getTimeSlot().isBefore(LocalDateTime.now())) {
            log.warn("Attempt to cancel past appointment {} for user {}", appointmentId, username);
            throw new IllegalStateException("Cannot cancel an appointment whose time has already passed.");
        }
        // Example: Add a rule for cancellation window (e.g., 24 hours before)
        // if (appointment.getSlot() != null && appointment.getSlot().getTimeSlot().isBefore(LocalDateTime.now().plusHours(24))) {
        //     log.warn("Attempt to cancel appointment {} for user {} too close to scheduled time {}", appointmentId, username, appointment.getSlot().getTimeSlot());
        //     throw new IllegalStateException("Appointments cannot be cancelled less than 24 hours in advance.");
        // }

        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        Slot slot = appointment.getSlot();
        if (slot != null) {
            slot.setSlotStatus(SlotStatus.AVAILABLE);
            slot.setAppointment(null); // Unlink appointment from slot
            slotRepository.save(slot);
        }
        log.info("Successfully cancelled appointment ID: {} for user: {}", appointmentId, username);
    }
}
