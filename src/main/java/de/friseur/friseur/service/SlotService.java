package de.friseur.friseur.service;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.AppointmentRepository;
import de.friseur.friseur.repository.SlotRepository;
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


    public SlotService(SlotRepository slotRepository, AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

    public List<LocalDateTime> getAllAvailableSlots() {
        try {
            logger.info("Getting all available slots");
            LocalDateTime today = LocalDateTime.now();
            List<Slot> availableSlots = slotRepository.findAllAvailableSlots(today, SlotStatus.AVAILABLE);

            return availableSlots.stream()
                    .map(Slot::getTimeSlot)  // Get the time slot
                    .sorted()  // Sort in natural (chronological) order
                    .collect(Collectors.toList());  // Collect as a list of LocalDateTime
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
                    .map(Slot::getTimeSlot)  // Get the time slot
                    .map(LocalDateTime::toLocalDate)  // Convert to LocalDate
                    .distinct()  // Remove duplicates
                    .sorted()  // Sort in natural (chronological) order
                    .toList();  // Collect as a list of LocalDate
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

            // 1. Create a new Appointment
            Appointment newAppointment = new Appointment();
            newAppointment.setUserId(userId);
            newAppointment.setClientName(clientName);
            newAppointment.setServiceType(serviceType);
            newAppointment.setCreatedAt(LocalDateTime.now());
            newAppointment.setAppointmentStatus("BOOKED"); // Or "PENDING" depending on your flow
            newAppointment.setSlot(slot); // Set the slot for the appointment

            // 2. Save the Appointment to get the generated ID
            newAppointment = appointmentRepository.save(newAppointment);

            // 3. Update the Slot with the new Appointment
            slot.setSlotStatus(SlotStatus.RESERVED);
            slot.setAppointment(newAppointment); // Link the appointment to the slot
            slotRepository.save(slot);

            logger.info("Slot at {} reserved and appointment created with ID {}", timeSlot, newAppointment.getAppointmentId());
            return true;
        } catch (Exception e) {
            logger.error("Error while reserving slot at {}", timeSlot, e);
            return false;
        }
    }
}
