package project.badminton.booking.dto;

import project.badminton.booking.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long customerId,
        String customerName,
        Long courtId,
        String courtName,
        Long timeSlotId,
        String timeRange,
        LocalDate bookingDate,
        BookingStatus status,
        String note,
        LocalDateTime createdAt
) {
}
