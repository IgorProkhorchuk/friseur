package de.friseur.friseur.controller;

import de.friseur.friseur.service.AppointmentService;
import de.friseur.friseur.service.exception.AppointmentNotFoundException;
import de.friseur.friseur.service.exception.UnauthorizedCancelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user/appointments") // Base path for user appointment actions
public class UserAppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public UserAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    public String showUserDashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        // Fetch upcoming appointments for logged-in user
        var appointments = appointmentService.getUpcomingAppointmentsForUser(username);
        model.addAttribute("appointments", appointments);

        return "dashboard"; // Name of Thymeleaf template
    }


    @PostMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId,
                                               Authentication authentication,
                                               @RequestHeader(value = "HX-Request", required = false) String hxRequest) {

        if (authentication == null || !authentication.isAuthenticated()) {
            // This should ideally be caught by Spring Security's configuration before reaching here.
            // If an unauthenticated request reaches this point, it's a configuration concern.
            if (isHtmxRequest(hxRequest)) {
                // For HTMX, sending a 401 Unauthorized and an HX-Redirect header
                // can instruct the client to navigate to the login page.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .header("HX-Redirect", "/login")
                                     .build();
            }
            // For non-HTMX requests, Spring Security would typically handle the redirect to login.
            // This is a fallback.
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/login").build();
        }

        String currentUsername = authentication.getName(); // Get username for authorization checks

        try {
            // The service method should verify that the appointment belongs to the current user
            appointmentService.cancelUserAppointment(appointmentId, currentUsername);

            if (isHtmxRequest(hxRequest)) {
                // For HTMX, a 200 OK with an empty body will allow hx-swap="outerHTML"
                // to remove the targeted element (the table row).
                return ResponseEntity.ok().build();
            } else {
                // For standard form submissions (noscript fallback), redirect to the dashboard.
                // A query parameter can optionally indicate success.
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user-dashboard?cancelSuccess=true")
                                     .build();
            }
        } catch (AppointmentNotFoundException e) {
            // Log the exception: log.error("Appointment not found: {}", appointmentId, e);
            if (isHtmxRequest(hxRequest)) {
                // For HTMX, returning a non-2xx status will prevent the swap by default.
                // The row will remain. You could also return an HTML snippet with an error message.
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found.");
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user-dashboard?error=notFound")
                                     .build();
            }
        } catch (UnauthorizedCancelException e) {
            // Log the exception: log.error("User {} unauthorized to cancel appointment {}", currentUsername, appointmentId, e);
            if (isHtmxRequest(hxRequest)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this appointment.");
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user-dashboard?error=unauthorized")
                                     .build();
            }
        } catch (Exception e) {
            // Log the exception: log.error("Error cancelling appointment {}: {}", appointmentId, e.getMessage(), e);
            if (isHtmxRequest(hxRequest)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user-dashboard?error=generic")
                                     .build();
            }
        }
    }

    private boolean isHtmxRequest(String hxRequestHeader) {
        return hxRequestHeader != null && hxRequestHeader.equalsIgnoreCase("true");
    }

}
