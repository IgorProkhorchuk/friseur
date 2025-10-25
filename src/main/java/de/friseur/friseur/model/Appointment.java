package de.friseur.friseur.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY) // Many appointments can belong to one user
    @JoinColumn(name = "user_id") // This will be the foreign key column in the 'appointment' table
    private User user;

    private String clientName;
    private String serviceType;
    @OneToOne(mappedBy = "appointment")
    private Slot slot;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    public Appointment(Long appointmentId, User user, String clientName, String serviceType, Slot slot, LocalDateTime createdAt, AppointmentStatus appointmentStatus) {
        this.appointmentId = appointmentId;
        this.user = user; // Use the User object
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

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public User getUser() { // Getter for User
        return user;
    }

    public void setUser(User user) { // Setter for User
        this.user = user;
    }
}