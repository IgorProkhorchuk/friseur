package de.friseur.friseur.friseur.service;

import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.ScheduleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void getLatestSchedule_shouldReturnLatestSchedule() {
        Schedule schedule = new Schedule();
        when(scheduleRepository.findTopByOrderByIdDesc()).thenReturn(schedule);

        Schedule result = scheduleService.getLatestSchedule();

        assertEquals(schedule, result);
        verify(scheduleRepository).findTopByOrderByIdDesc();
    }

    @Test
    void createDateRange_shouldCreateCorrectDateRange() {
        Schedule schedule = new Schedule();
        schedule.setStartDate(LocalDate.of(2025, 1, 1));
        schedule.setEndDate(LocalDate.of(2025, 1, 3));

        List<LocalDateTime> dateRange = scheduleService.createDateRange(schedule);

        assertEquals(3, dateRange.size());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), dateRange.get(0));
        assertEquals(LocalDateTime.of(2025, 1, 2, 0, 0), dateRange.get(1));
        assertEquals(LocalDateTime.of(2025, 1, 3, 0, 0), dateRange.get(2));
    }

    @Test
    void createTimeslots_shouldCreateCorrectTimeslots() {
        List<LocalDateTime> dateRange = Arrays.asList(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0)
        );

        List<LocalDateTime> timeslots = scheduleService.createTimeslots(dateRange);

        assertEquals(24, timeslots.size()); // 2 days * 12 hours
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), timeslots.get(0));
        assertEquals(LocalDateTime.of(2025, 1, 1, 20, 0), timeslots.get(11));
        assertEquals(LocalDateTime.of(2025, 1, 2, 9, 0), timeslots.get(12));
        assertEquals(LocalDateTime.of(2025, 1, 2, 20, 0), timeslots.get(23));
    }

    @Test
    void saveSelectedTimeslots_shouldSaveCorrectSlots() {
        List<String> selectedTimeslots = Arrays.asList(
                "2025-01-01 09:00",
                "2025-01-01 10:00"
        );

        scheduleService.saveSelectedTimeslots(selectedTimeslots);

        ArgumentCaptor<List<Slot>> captor = ArgumentCaptor.forClass(List.class);
        verify(slotRepository).saveAll(captor.capture());

        List<Slot> savedSlots = captor.getValue();
        assertEquals(2, savedSlots.size());
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), savedSlots.get(0).getTimeSlot());
        assertEquals(SlotStatus.AVAILABLE, savedSlots.get(0).getSlotStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), savedSlots.get(1).getTimeSlot());
        assertEquals(SlotStatus.AVAILABLE, savedSlots.get(1).getSlotStatus());
    }
}
