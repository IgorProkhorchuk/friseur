package de.friseur.friseur.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;
    private LocalDate date;
    private String weekDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String slotStatus;
    @OneToOne
    @JoinColumn(name = "appointmentId")
    private Appointment appointment;

    public Slot(int slotId, LocalDate date, String weekDay, LocalTime startTime, LocalTime endTime, String slotStatus, Appointment appointment) {
        this.slotId = slotId;
        this.date = date;
        this.weekDay = weekDay;
        this.startTime = startTime;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
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
