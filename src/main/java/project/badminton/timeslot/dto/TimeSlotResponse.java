package project.badminton.timeslot.dto;

import java.time.LocalTime;

public record TimeSlotResponse(
        Long id,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}
