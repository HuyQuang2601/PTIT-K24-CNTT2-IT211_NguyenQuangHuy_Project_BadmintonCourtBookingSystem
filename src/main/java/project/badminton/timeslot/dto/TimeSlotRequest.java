package project.badminton.timeslot.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeSlotRequest(
        @NotNull(message = "Thời gian bắt đầu không được để trống") LocalTime startTime,
        @NotNull(message = "Thời gian kết thúc không được để trống") LocalTime endTime,
        boolean active
) {
}
