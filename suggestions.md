# Suggestions for Improving the Friseur Application

## Introduction

The current application logic allows an administrator to create duplicate schedules and time slots. This can lead to inconsistencies in the booking system and a poor user experience. This document outlines several suggestions to prevent these issues and improve the overall robustness of the application.

## 1. Preventing Duplicate Schedules

The application currently allows creating multiple `Schedule` entries for the same or overlapping date ranges.

### Suggestion 1.1: Add Validation in the Service Layer

Before saving a new `Schedule`, check if any existing schedules overlap with the new one.

**1. In `ScheduleRepository.java`, add a method to find overlapping schedules:**

```java
// src/main/java/de/friseur/friseur/repository/ScheduleRepository.java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findTopByOrderByIdDesc();

    // Add this method
    @Query("SELECT s FROM Schedule s WHERE s.startDate <= :endDate AND s.endDate >= :startDate")
    List<Schedule> findOverlappingSchedules(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
```

**2. In `ScheduleService.java`, add a method to save a schedule with validation:**

```java
// src/main/java/de/friseur/friseur/service/ScheduleService.java

public boolean saveSchedule(Schedule schedule) {
    List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(schedule.getStartDate(), schedule.getEndDate());
    if (overlappingSchedules.isEmpty()) {
        scheduleRepository.save(schedule);
        return true; // Success
    }
    return false; // Overlapping schedule exists
}
```

**3. In `ScheduleController.java`, use the new service method:**

```java
// src/main/java/de/friseur/friseur/controller/ScheduleController.java

@PostMapping("/admin")
public String setSchedule(@ModelAttribute Schedule schedule,
                          Model model,
                          @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
    logger.info("Received startDate: {}", schedule.getStartDate());
    logger.info("Received endDate: {}", schedule.getEndDate());

    if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
        model.addAttribute("errorMessage", "Start and End Dates are required");
        return isHx(hxRequest) ? "admin :: set-schedule" : "admin";
    }

    if (scheduleService.saveSchedule(schedule)) {
        model.addAttribute("successMessage", "Dates added successfully");
    } else {
        model.addAttribute("errorMessage", "A schedule with overlapping dates already exists.");
    }

    return isHx(hxRequest) ? "admin :: set-schedule" : "admin";
}
```

### Suggestion 1.2: Add a Database Constraint

For a stronger guarantee, you can add a unique constraint to the database. However, this is more complex for overlapping date ranges and might require more advanced database features or a different table structure. For now, the service layer validation is a good and flexible solution.

## 2. Preventing Duplicate Slots

The application allows creating multiple `Slot` entries for the exact same `LocalDateTime`.

### Suggestion 2.1: Add Validation in `ScheduleService`

Before saving new slots, check if a slot for that `LocalDateTime` already exists.

**In `ScheduleService.java`, modify `saveSelectedTimeslots`:**

```java
// src/main/java/de/friseur/friseur/service/ScheduleService.java

public void saveSelectedTimeslots(List<String> selectedTimeslots) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    List<Slot> newSlots = new ArrayList<>();
    for (String timeSlotStr : selectedTimeslots) {
        LocalDateTime timeSlot = LocalDateTime.parse(timeSlotStr, formatter);
        // Check if a slot at this time already exists
        if (slotRepository.findByTimeSlot(timeSlot) == null) {
            Slot slot = new Slot();
            slot.setTimeSlot(timeSlot);
            slot.setSlotStatus(SlotStatus.AVAILABLE);
            slot.setAppointment(null);
            newSlots.add(slot);
        }
    }

    if (!newSlots.isEmpty()) {
        slotRepository.saveAll(newSlots);
    }
}
```
This will silently ignore duplicate slots. To provide feedback, you could return the number of created slots or a list of duplicates.

### Suggestion 2.2: Add a Database Constraint

A more robust solution is to add a unique constraint on the `timeSlot` column in the `Slot` table. This will prevent duplicate slots at the database level.

**In `Slot.java`, add the `@UniqueConstraint` annotation:**

```java
// src/main/java/de/friseur/friseur/model/Slot.java
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "timeSlot")
})
public class Slot {
    // ... existing code
}
```
When using this approach, you will need to handle the `DataIntegrityViolationException` that will be thrown if you try to insert a duplicate slot.

## 3. Improving User Feedback

When an admin tries to create a duplicate schedule or slots, they should be clearly informed. My suggestion for the `ScheduleController` already includes an error message for duplicate schedules. You can enhance this by also providing feedback for duplicate slots.

## 4. Refactoring Suggestion

### Move Business Logic to the Service Layer

The logic for saving a `Schedule` is currently in the `ScheduleController`. It's a good practice to move business logic to the service layer to keep controllers thin and focused on handling HTTP requests. The example in Suggestion 1.1 already demonstrates this by introducing a `saveSchedule` method in the `ScheduleService`.
