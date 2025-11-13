package de.friseur.friseur.model;

/**
 * Describes the booking state of a discrete time slot.
 */
public enum SlotStatus {
    AVAILABLE,
    RESERVED,
    PAID,
    CANCELLED,
    CLOSED,
    HIDDEN
}
