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

/**
 * Exposes authenticated user operations related to their appointments such as viewing
 * upcoming bookings and cancelling individual slots via standard or HTMX requests.
 */
@Controller
@RequestMapping("/user/appointments") // Base path for user appointment actions
public class UserAppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Creates the controller with the required appointment service dependency.
     *
     * @param appointmentService business logic for appointment reads/cancellations
     */
    @Autowired
    public UserAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Displays the authenticated user's dashboard view populated with future appointments.
     *
     * @param authentication Spring Security context
     * @param model          dashboard model attributes
     * @return redirect to login when unauthenticated or the dashboard view otherwise
     */
    @GetMapping("/dashboard")
    public String showUserDashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        // Fetch upcoming appointments for logged-in user
        var appointments = appointmentService.getUpcomingAppointmentsForUser(username);
        model.addAttribute("appointments", appointments);

        return "dashboard";
    }


    /**
     * Cancels one of the user's appointments, adapting the response format depending on
     * whether the request originated from HTMX or a classic browser submission.
     *
     * @param appointmentId id of the appointment to cancel
     * @param authentication security context for authorization checks
     * @param hxRequest optional HTMX header
     * @return appropriate HTTP response/redirect signaling the outcome
     */
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
                                     .header("Location", "/user/appointments/dashboard?cancelSuccess=true")
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
                                     .header("Location", "/user/appointments/dashboard?error=notFound")
                                     .build();
            }
        } catch (UnauthorizedCancelException e) {
            // Log the exception: log.error("User {} unauthorized to cancel appointment {}", currentUsername, appointmentId, e);
            if (isHtmxRequest(hxRequest)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this appointment.");
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user/appointments/dashboard?error=unauthorized")
                                     .build();
            }
        } catch (Exception e) {
            // Log the exception: log.error("Error cancelling appointment {}: {}", appointmentId, e.getMessage(), e);
            if (isHtmxRequest(hxRequest)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                                     .header("Location", "/user/appointments/dashboard?error=generic")
                                     .build();
            }
        }
    }

    /**
     * Detects HTMX requests so the controller can return HX-friendly responses.
     *
     * @param hxRequestHeader incoming HX-Request header value
     * @return {@code true} when the header explicitly states a HTMX call
     */
    private boolean isHtmxRequest(String hxRequestHeader) {
        return hxRequestHeader != null && hxRequestHeader.equalsIgnoreCase("true");
    }

}
