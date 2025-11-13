package de.friseur.friseur.service.exception;

/**
 * Signals that an appointment lookup failed to find a matching record.
 */
public class AppointmentNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     */
    public AppointmentNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and underlying cause for context.
     */
    public AppointmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
