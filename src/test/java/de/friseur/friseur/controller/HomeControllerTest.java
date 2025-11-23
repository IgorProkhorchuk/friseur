package de.friseur.friseur.controller;

import de.friseur.friseur.config.SecurityConfig;
import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import de.friseur.friseur.service.AppointmentService;
import de.friseur.friseur.service.SlotService;
import de.friseur.friseur.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import({HomeControllerTest.TestConfig.class, SecurityConfig.class})
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SlotService slotService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SlotService slotService() {
            return mock(SlotService.class);
        }

        @Bean
        public AppointmentService appointmentService() {
            return mock(AppointmentService.class);
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

    @Test
    void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testHome_withUser() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("userName", "testuser"));
    }

    @Test
    void testShop() throws Exception {
        mockMvc.perform(get("/shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("shop"));
    }

    @Test
    void testGetAllAvailableSlots() throws Exception {
        List<LocalDate> mockDates = Arrays.asList(LocalDate.now().plusDays(1));
        List<LocalDateTime> mockSlots = Arrays.asList(LocalDateTime.now().plusHours(1));

        when(slotService.getAllAvailableDates()).thenReturn(mockDates);
        when(slotService.getAllAvailableSlots()).thenReturn(mockSlots);

        mockMvc.perform(get("/slots"))
                .andExpect(status().isOk())
                .andExpect(view().name("slots"))
                .andExpect(model().attribute("dates", mockDates))
                .andExpect(model().attribute("slots", mockSlots));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testReserveSlot_success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(slotService.reserveSlot(any(LocalDateTime.class), any(Integer.class), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/slots")
                        .with(csrf())
                        .param("slot", "2024-01-01 10:00:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/confirmation"))
                .andExpect(model().attribute("message", "Slot reserved successfully"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testReserveSlot_failure() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(slotService.reserveSlot(any(LocalDateTime.class), any(Integer.class), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/slots")
                        .with(csrf())
                        .param("slot", "2024-01-01 10:00:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/confirmation"))
                .andExpect(model().attribute("message", "Slot reservation failed"));
    }
}
