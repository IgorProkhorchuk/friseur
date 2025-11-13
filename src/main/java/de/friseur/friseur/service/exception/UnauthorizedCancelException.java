package de.friseur.friseur.service.exception;

/**
 * Thrown when a user attempts to cancel an appointment they do not own.
 */
public class UnauthorizedCancelException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     */
    public UnauthorizedCancelException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and underlying cause for context.
     */
    public UnauthorizedCancelException(String message, Throwable cause) {
        super(message, cause);
    }
}
