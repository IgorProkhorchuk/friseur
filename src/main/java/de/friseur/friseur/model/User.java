package de.friseur.friseur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Set;


@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userName;
    private String userPhone;
    @NotNull
    private String password;

    @OneToMany
    @JoinColumn(name = "appointmentId")
    private Set<Appointment> appointment;

    public User() {
    }

    public User(int userId, String userName, String userPhone, String password, Set<Appointment> appointment) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.appointment = appointment;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Appointment> getAppointment() {
        return appointment;
    }

    public void setAppointment(Set<Appointment> appointment) {
        this.appointment = appointment;
    }
}
