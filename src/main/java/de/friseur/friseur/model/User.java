package de.friseur.friseur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Entity
@Table(name = "app_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    // Ensure usernames are unique, disable once email verification is implemented
    @Column(unique = true, name = "user_name")
    private String username;

    @Column(unique = true)
    private String userPhone;

    @NotNull
    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // Store roles in a separate table
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @OneToMany
    @JoinColumn(name = "appointmentId")
    private Set<Appointment> appointment;

    public User() {
    }

    public User(int userId, String username, String userPhone, String password, Set<String> roles, Set<Appointment> appointment) {
        this.userId = userId;
        this.username = username;
        this.userPhone = userPhone;
        this.password = password;
        this.roles = roles;
        this.appointment = appointment;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    // Spring Security methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
