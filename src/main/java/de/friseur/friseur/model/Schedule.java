package de.friseur.friseur.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;


@Entity
@Table
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Start Date cannot be null")
    private LocalDate startDate;
    @NotNull(message = "End Date cannot be null")
    private LocalDate endDate;

    public Schedule() {
    }

    public Schedule(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
