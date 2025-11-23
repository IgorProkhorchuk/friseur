package de.friseur.friseur.friseur.service;

import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.AppointmentStatus;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.AppointmentRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.AppointmentServiceImpl;
import de.friseur.friseur.service.exception.AppointmentNotFoundException;
import de.friseur.friseur.service.exception.UnauthorizedCancelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void getUpcomingAppointmentsForUser_shouldReturnUpcomingAppointments() {
        String email = "test@example.com";
        Appointment appointment = new Appointment();
        when(appointmentRepository.findByUser_EmailAndAppointmentStatusAndSlot_TimeSlotAfterOrderBySlot_TimeSlotAsc(
                eq(email), eq(AppointmentStatus.UPCOMING), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(appointment));

        List<Appointment> appointments = appointmentService.getUpcomingAppointmentsForUser(email);

        assertFalse(appointments.isEmpty());
        assertEquals(1, appointments.size());
        verify(appointmentRepository).findByUser_EmailAndAppointmentStatusAndSlot_TimeSlotAfterOrderBySlot_TimeSlotAsc(
                eq(email), eq(AppointmentStatus.UPCOMING), any(LocalDateTime.class));
    }

    @Test
    void cancelUserAppointment_shouldCancelAppointmentSuccessfully() throws AppointmentNotFoundException, UnauthorizedCancelException {
        String email = "test@example.com";
        Long appointmentId = 1L;
        User user = new User();
        user.setEmail(email);
        Slot slot = new Slot();
        slot.setTimeSlot(LocalDateTime.now().plusDays(1));
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setUser(user);
        appointment.setSlot(slot);
        appointment.setAppointmentStatus(AppointmentStatus.UPCOMING);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.cancelUserAppointment(appointmentId, email);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getAppointmentStatus());
        verify(appointmentRepository).save(appointment);
        assertEquals(SlotStatus.AVAILABLE, slot.getSlotStatus());
        assertNull(slot.getAppointment());
        verify(slotRepository).save(slot);
    }

    @Test
    void cancelUserAppointment_shouldThrowAppointmentNotFoundException() {
        Long appointmentId = 1L;
        String email = "test@example.com";
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> {
            appointmentService.cancelUserAppointment(appointmentId, email);
        });
    }

    @Test
    void cancelUserAppointment_shouldThrowUnauthorizedCancelException() {
        String email = "test@example.com";
        String ownerEmail = "owner@example.com";
        Long appointmentId = 1L;
        User owner = new User();
        owner.setEmail(ownerEmail);
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setUser(owner);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(UnauthorizedCancelException.class, () -> {
            appointmentService.cancelUserAppointment(appointmentId, email);
        });
    }

    @Test
    void cancelUserAppointment_shouldThrowIllegalStateException_whenAppointmentNotUpcoming() {
        String email = "test@example.com";
        Long appointmentId = 1L;
        User user = new User();
        user.setEmail(email);
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setUser(user);
        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> {
            appointmentService.cancelUserAppointment(appointmentId, email);
        });
    }

    @Test
    void cancelUserAppointment_shouldThrowIllegalStateException_whenSlotIsInThePast() {
        String email = "test@example.com";
        Long appointmentId = 1L;
        User user = new User();
        user.setEmail(email);
        Slot slot = new Slot();
        slot.setTimeSlot(LocalDateTime.now().minusDays(1));
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setUser(user);
        appointment.setSlot(slot);
        appointment.setAppointmentStatus(AppointmentStatus.UPCOMING);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () -> {
            appointmentService.cancelUserAppointment(appointmentId, email);
        });
    }
}
