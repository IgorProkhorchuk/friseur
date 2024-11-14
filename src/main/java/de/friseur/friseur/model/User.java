package de.friseur.friseur.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userName;
    private String userPhone;

    @OneToMany(mappedBy = "user")
    private Set<Bookings> bookings = new HashSet<>();

    public User(int userId, String userName, String userPhone, Set<Bookings> bookings) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.bookings = bookings;
    }

    public User() {
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
}
