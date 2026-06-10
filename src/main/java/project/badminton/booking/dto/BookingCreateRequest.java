package project.badminton.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookingCreateRequest(
        @NotNull Long courtId,
        @NotNull @FutureOrPresent LocalDate bookingDate,
        @NotNull Long timeSlotId,
        @Size(max = 500) String note
) {
}
