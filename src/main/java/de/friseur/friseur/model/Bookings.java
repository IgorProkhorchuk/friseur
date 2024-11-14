package de.friseur.friseur.model;

import jakarta.persistence.*;

@Entity
@Table
public class Bookings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingsId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private TimeSlot timeSlot;

    public Bookings(int bookingsId, User user, TimeSlot timeSlot) {
        this.bookingsId = bookingsId;
        this.user = user;
        this.timeSlot = timeSlot;
    }

    public int getBookingsId() {
        return bookingsId;
    }

    public void setBookingsId(int bookingsId) {
        this.bookingsId = bookingsId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
}
