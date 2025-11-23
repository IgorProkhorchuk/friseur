package de.friseur.friseur.service;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.AppointmentRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SlotService {
    private static final Logger logger = LoggerFactory.getLogger(SlotService.class);
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository; // Inject UserRepository

    public SlotService(SlotRepository slotRepository, AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository; // Initialize UserRepository
    }

    public List<LocalDateTime> getAllAvailableSlots() {
        try {
            logger.info("Getting all available slots");
            LocalDateTime today = LocalDateTime.now();
            List<Slot> availableSlots = slotRepository.findAllAvailableSlots(today, SlotStatus.AVAILABLE);

            return availableSlots.stream()
                    .map(Slot::getTimeSlot)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while getting available slots", e);
            return Collections.emptyList();
        }
    }

    public List<LocalDate> getAllAvailableDates() {
        try {
            logger.info("Getting all available dates");
            LocalDateTime now = LocalDateTime.now();
            List<Slot> availableSlots = slotRepository.findAllAvailableSlots(now, SlotStatus.AVAILABLE);

            return availableSlots.stream()
                    .map(Slot::getTimeSlot)
                    .map(LocalDateTime::toLocalDate)
                    .distinct()
                    .sorted()
                    .toList();
        } catch (NullPointerException e) {
            logger.error("NullPointerException caught while getting available dates", e);
            return List.of();
        }
    }

    public boolean reserveSlot(LocalDateTime timeSlot, int userId, String clientName, String serviceType) {
        try {
            logger.info("Reserving slot at {}", timeSlot);
            Slot slot = slotRepository.findByTimeSlot(timeSlot);
            if (slot == null) {
                logger.warn("Slot not found at {}", timeSlot);
                return false;
            }

            // Fetch the User entity
            User user = userRepository.findById(userId)
                                       .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            // 1. Create a new Appointment
            Appointment newAppointment = new Appointment();
            newAppointment.setUser(user); // Set the User object
            newAppointment.setClientName(clientName);
            newAppointment.setServiceType(serviceType);
            newAppointment.setCreatedAt(LocalDateTime.now());
            newAppointment.setAppointmentStatus(AppointmentStatus.UPCOMING);
            newAppointment.setSlot(slot);

            // 2. Save the Appointment to get the generated ID
            newAppointment = appointmentRepository.save(newAppointment);

            // 3. Add the new appointment to the user's appointments set
            user.getAppointment().add(newAppointment);
            userRepository.save(user); // Save the user to update the relationship

            // 4. Update the Slot with the new Appointment
            slot.setSlotStatus(SlotStatus.RESERVED);
            slot.setAppointment(newAppointment);
            slotRepository.save(slot);

            logger.info("Slot at {} reserved and appointment created with ID {}", timeSlot, newAppointment.getAppointmentId());
            return true;
        } catch (Exception e) {
            logger.error("Error while reserving slot at {}", timeSlot, e);
            return false;
        }
    }
}
