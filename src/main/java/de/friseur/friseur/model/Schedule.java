package de.friseur.friseur.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
@Table
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Start Date cannot be null")
    private String startDate;
    @NotNull(message = "End Date cannot be null")
    private String endDate;

    public Schedule() {
    }

    public Schedule(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
