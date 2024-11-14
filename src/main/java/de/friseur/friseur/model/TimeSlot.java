package de.friseur.friseur.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;
    private String date;
    private String weekDay;
    private String time;
    private boolean bookedSlot;
    @OneToMany(mappedBy = "timeSlot")
    private Set<Bookings> bookings = new HashSet<>();

    public TimeSlot(int slotId, String date, String weekDay, String time, boolean bookedSlot, Set<Bookings> bookings) {
        this.slotId = slotId;
        this.date = date;
        this.weekDay = weekDay;
        this.time = time;
        this.bookedSlot = bookedSlot;
        this.bookings = bookings;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isBookedSlot() {
        return bookedSlot;
    }

    public void setBookedSlot(boolean bookedSlot) {
        this.bookedSlot = bookedSlot;
    }

    public Set<Bookings> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Bookings> bookings) {
        this.bookings = bookings;
    }
}
