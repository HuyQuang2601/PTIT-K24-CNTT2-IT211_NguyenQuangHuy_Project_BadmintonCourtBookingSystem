package project.badminton.timeslot.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeSlotRequest(
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        boolean active
) {
}
