package de.friseur.friseur.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;
    private String clientName;
    private String serviceType;
    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;
    private LocalDateTime createdAt;
    private String appointmentStatus;

    public Appointment(Long appointmentId, String clientName, String serviceType, Slot slot, LocalDateTime createdAt, String appointmentStatus) {
        this.appointmentId = appointmentId;
        this.clientName = clientName;
        this.serviceType = serviceType;
        this.slot = slot;
        this.createdAt = createdAt;
        this.appointmentStatus = appointmentStatus;
    }

    public Appointment() {
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }
}
