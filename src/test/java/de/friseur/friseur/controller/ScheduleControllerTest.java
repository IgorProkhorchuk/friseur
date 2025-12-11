package de.friseur.friseur.controller;

import de.friseur.friseur.config.JwtConfig;
import de.friseur.friseur.config.SecurityConfig;
import de.friseur.friseur.model.Schedule;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.model.SlotStatus;
import de.friseur.friseur.repository.ScheduleRepository;
import de.friseur.friseur.repository.SlotRepository;
import de.friseur.friseur.service.ScheduleService;
import de.friseur.friseur.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
@Import({SecurityConfig.class, JwtConfig.class})
@WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private SlotRepository slotRepository;

    @MockBean
    private de.friseur.friseur.service.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void admin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    void setSchedule_success() throws Exception {
        when(scheduleService.saveSchedule(any(Schedule.class))).thenReturn(true);

        mockMvc.perform(post("/admin")
                        .with(csrf())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("successMessage", "Dates added successfully"));
    }

    @Test
    void saveSelectedTimeslots() throws Exception {
        mockMvc.perform(post("/admin/save-schedule")
                        .with(csrf())
                        .param("selectedTimeslots", "2024-01-01 10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/success"));
    }

    @Test
    void viewBookedSlots() throws Exception {
        Slot slot = new Slot();
        slot.setSlotStatus(SlotStatus.RESERVED);
        when(slotRepository.findAll()).thenReturn(Collections.singletonList(slot));

        mockMvc.perform(get("/admin/booked"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-booked"))
                .andExpect(model().attributeExists("bookedSlots"));
    }

    @Test
    void toggleSlot() throws Exception {
        Slot slot = new Slot();
        slot.setSlotId(1);
        slot.setSlotStatus(SlotStatus.AVAILABLE);
        when(slotRepository.findById(1)).thenReturn(Optional.of(slot));

        mockMvc.perform(post("/admin/slots/toggle/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/slots/manage"));
    }
}
