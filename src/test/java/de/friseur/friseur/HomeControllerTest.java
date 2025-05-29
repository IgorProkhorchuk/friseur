package de.friseur.friseur;

import de.friseur.friseur.controller.HomeController;
import de.friseur.friseur.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    @Mock
    private SlotService slotService;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHello() {
        // Act
        String viewName = homeController.hello();

        // Assert
        assertEquals("index", viewName);
    }

    @Test
    void testShop() {
        // Act
        String viewName = homeController.shop();

        // Assert
        assertEquals("shop", viewName);
    }

    @Test
    void testGetAllAvailableSlots() {
        // Arrange
        List<LocalDate> mockDates = Arrays.asList(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );
        List<LocalDateTime> mockSlots = Arrays.asList(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        when(slotService.getAllAvailableDates()).thenReturn(mockDates);
        when(slotService.getAllAvailableSlots()).thenReturn(mockSlots);

        // Act
        String viewName = homeController.getAllAvailableSlots(model);

        // Assert
        assertEquals("slots", viewName);
        verify(model).addAttribute("dates", mockDates);
        verify(model).addAttribute("slots", mockSlots);
    }
}