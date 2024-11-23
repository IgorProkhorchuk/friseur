package de.friseur.friseur.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;
    private LocalDateTime timeSlot;
    private String slotStatus;
    @OneToOne
    @JoinColumn(name = "appointmentId")
    private Appointment appointment;

    public Slot(int slotId, LocalDateTime timeSlot, String slotStatus, Appointment appointment) {
        this.slotId = slotId;
        this.timeSlot = timeSlot;
        this.slotStatus = slotStatus;
        this.appointment = appointment;
    }

    public Slot() {
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocalDateTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(String slotStatus) {
        this.slotStatus = slotStatus;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
}
