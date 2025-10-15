package de.friseur.friseur.service.exception;

public class UnauthorizedCancelException extends RuntimeException {

    public UnauthorizedCancelException(String message) {
        super(message);
    }

    public UnauthorizedCancelException(String message, Throwable cause) {
        super(message, cause);
    }
}
