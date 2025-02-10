package de.friseur.friseur;

import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private SlotService slotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAvailableSlots_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Slot slot1 = createSlot(now.plusHours(1));
        Slot slot2 = createSlot(now.plusHours(2));

        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE)))
                .thenReturn(Arrays.asList(slot1, slot2));

        // Act
        List<LocalDateTime> availableSlots = slotService.getAllAvailableSlots();

        // Assert
        assertNotNull(availableSlots);
        assertEquals(2, availableSlots.size());
        verify(slotRepository).findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE));
    }

    @Test
    void testGetAllAvailableSlots_Exception() {
        // Arrange
        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<LocalDateTime> availableSlots = slotService.getAllAvailableSlots();

        // Assert
        assertTrue(availableSlots.isEmpty());
    }

    @Test
    void testGetAllAvailableDates_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Slot slot1 = createSlot(now.plusDays(1));
        Slot slot2 = createSlot(now.plusDays(2));
        Slot slot3 = createSlot(now.plusDays(1).plusHours(1)); // Same date as slot1

        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE)))
                .thenReturn(Arrays.asList(slot1, slot2, slot3));

        // Act
        List<LocalDate> availableDates = slotService.getAllAvailableDates();

        // Assert
        assertNotNull(availableDates);
        assertEquals(2, availableDates.size()); // Distinct dates
        verify(slotRepository).findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE));
    }

    @Test
    void testGetAllAvailableDates_EmptyList() {
        // Arrange
        when(slotRepository.findAllAvailableSlots(any(LocalDateTime.class), eq(SlotStatus.AVAILABLE)))
                .thenReturn(Collections.emptyList());

        // Act
        List<LocalDate> availableDates = slotService.getAllAvailableDates();

        // Assert
        assertTrue(availableDates.isEmpty());
    }

    @Test
    void testReserveSlot_Success() {
        // Arrange
        LocalDateTime timeSlot = LocalDateTime.now().plusHours(1);
        Slot slot = createSlot(timeSlot);

        when(slotRepository.findByTimeSlot(timeSlot)).thenReturn(slot);

        // Act
        boolean result = slotService.reserveSlot(timeSlot);

        // Assert
        assertTrue(result);
        assertEquals(SlotStatus.RESERVED, slot.getSlotStatus());
        verify(slotRepository).save(slot);
    }

    @Test
    void testReserveSlot_SlotNotFound() {
        // Arrange
        LocalDateTime timeSlot = LocalDateTime.now().plusHours(1);

        when(slotRepository.findByTimeSlot(timeSlot)).thenReturn(null);

        // Act
        boolean result = slotService.reserveSlot(timeSlot);

        // Assert
        assertFalse(result);
        verify(slotRepository, never()).save(any());
    }

    private Slot createSlot(LocalDateTime timeSlot) {
        Slot slot = new Slot();
        slot.setTimeSlot(timeSlot);
        slot.setSlotStatus(SlotStatus.AVAILABLE);
        return slot;
    }
}