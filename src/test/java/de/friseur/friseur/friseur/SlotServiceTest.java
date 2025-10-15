package de.friseur.friseur.friseur;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.AppointmentRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.SlotService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SlotService slotService;

    @Test
    void getAllAvailableSlots_shouldReturnAvailableSlots() {
        Slot slot1 = new Slot();
        slot1.setTimeSlot(LocalDateTime.of(2025, 1, 1, 9, 0));
        Slot slot2 = new Slot();
        slot2.setTimeSlot(LocalDateTime.of(2025, 1, 1, 10, 0));
        List<Slot> slots = Arrays.asList(slot1, slot2);

        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), any(SlotStatus.class))).thenReturn(slots);

        List<LocalDateTime> result = slotService.getAllAvailableSlots();

        assertEquals(2, result.size());
        assertEquals(slot1.getTimeSlot(), result.get(0));
        assertEquals(slot2.getTimeSlot(), result.get(1));
    }

    @Test
    void getAllAvailableDates_shouldReturnDistinctDates() {
        Slot slot1 = new Slot();
        slot1.setTimeSlot(LocalDateTime.of(2025, 1, 1, 9, 0));
        Slot slot2 = new Slot();
        slot2.setTimeSlot(LocalDateTime.of(2025, 1, 1, 10, 0));
        Slot slot3 = new Slot();
        slot3.setTimeSlot(LocalDateTime.of(2025, 1, 2, 9, 0));
        List<Slot> slots = Arrays.asList(slot1, slot2, slot3);

        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), any(SlotStatus.class))).thenReturn(slots);

        List<LocalDate> result = slotService.getAllAvailableDates();

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 1, 1), result.get(0));
        assertEquals(LocalDate.of(2025, 1, 2), result.get(1));
    }

    @Test
    void reserveSlot_shouldReserveSlotSuccessfully() {
        LocalDateTime timeSlot = LocalDateTime.of(2025, 1, 1, 9, 0);
        int userId = 1;
        String username = "testuser";
        String serviceType = "Haircut";
        Slot slot = new Slot();
        slot.setTimeSlot(timeSlot);
        User user = new User();
        user.setUserId(userId);

        when(slotRepository.findByTimeSlot(timeSlot)).thenReturn(slot);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean result = slotService.reserveSlot(timeSlot, userId, username, serviceType);

        assertTrue(result);
        verify(slotRepository).save(slot);
        verify(userRepository).save(user);
        verify(appointmentRepository).save(any(Appointment.class));
        assertEquals(SlotStatus.RESERVED, slot.getSlotStatus());
        assertNotNull(slot.getAppointment());
    }

    @Test
    void reserveSlot_shouldReturnFalse_whenSlotNotFound() {
        LocalDateTime timeSlot = LocalDateTime.of(2025, 1, 1, 9, 0);
        when(slotRepository.findByTimeSlot(timeSlot)).thenReturn(null);

        boolean result = slotService.reserveSlot(timeSlot, 1, "testuser", "Haircut");

        assertFalse(result);
    }

    @Test
    void reserveSlot_shouldReturnFalse_whenUserNotFound() {
        LocalDateTime timeSlot = LocalDateTime.of(2025, 1, 1, 9, 0);
        int userId = 1;
        Slot slot = new Slot();
        slot.setTimeSlot(timeSlot);

        when(slotRepository.findByTimeSlot(timeSlot)).thenReturn(slot);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = slotService.reserveSlot(timeSlot, userId, "testuser", "Haircut");

        assertFalse(result);
    }
}
