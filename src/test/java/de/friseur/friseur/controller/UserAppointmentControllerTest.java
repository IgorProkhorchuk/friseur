package de.friseur.friseur.controller;

import de.friseur.friseur.config.JwtConfig;
import de.friseur.friseur.config.SecurityConfig;
import de.friseur.friseur.model.Appointment;
import de.friseur.friseur.model.Slot;
import de.friseur.friseur.service.AppointmentService;
import de.friseur.friseur.service.exception.AppointmentNotFoundException;
import de.friseur.friseur.service.exception.UnauthorizedCancelException;
import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAppointmentController.class)
@Import({UserAppointmentControllerTest.TestConfig.class, SecurityConfig.class, JwtConfig.class})
class UserAppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentService appointmentService;

    @MockBean
    private de.friseur.friseur.service.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppointmentService appointmentService() {
            return mock(AppointmentService.class);
        }
    }

    @Test
    void showUserDashboard_unauthenticated() throws Exception {
        mockMvc.perform(get("/user/appointments/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void showUserDashboard() throws Exception {
        Appointment appointment = new Appointment();
        Slot slot = new Slot();
        slot.setTimeSlot(LocalDateTime.of(2024, 1, 1, 10, 0));
        appointment.setSlot(slot);
        var user = new de.friseur.friseur.model.User();
        user.setUsername("user");
        when(userRepository.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(user));

        when(appointmentService.getUpcomingAppointmentsForUser("user@example.com"))
                .thenReturn(Collections.singletonList(appointment));

        mockMvc.perform(get("/user/appointments/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("appointments"))
                .andExpect(model().attribute("userName", "user"));
    }

    @Test
    void cancelAppointment_unauthenticated() throws Exception {
        mockMvc.perform(post("/user/appointments/cancel/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_htmx() throws Exception {
        mockMvc.perform(post("/user/appointments/cancel/1")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_noHtmx() throws Exception {
        mockMvc.perform(post("/user/appointments/cancel/1").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/appointments/dashboard?cancelSuccess=true"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_notFound_htmx() throws Exception {
        doThrow(new AppointmentNotFoundException("Not found")).when(appointmentService).cancelUserAppointment(1L, "user@example.com");

        mockMvc.perform(post("/user/appointments/cancel/1")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_notFound_noHtmx() throws Exception {
        doThrow(new AppointmentNotFoundException("Not found")).when(appointmentService).cancelUserAppointment(1L, "user@example.com");

        mockMvc.perform(post("/user/appointments/cancel/1").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/appointments/dashboard?error=notFound"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_unauthorized_htmx() throws Exception {
        doThrow(new UnauthorizedCancelException("Unauthorized")).when(appointmentService).cancelUserAppointment(1L, "user@example.com");

        mockMvc.perform(post("/user/appointments/cancel/1")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void cancelAppointment_unauthorized_noHtmx() throws Exception {
        doThrow(new UnauthorizedCancelException("Unauthorized")).when(appointmentService).cancelUserAppointment(1L, "user@example.com");

        mockMvc.perform(post("/user/appointments/cancel/1").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/appointments/dashboard?error=unauthorized"));
    }
}
