package project.badminton.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookingCreateRequest(
        @NotNull(message = "Mã sân không được để trống") Long courtId,
        @NotNull(message = "Ngày đặt sân không được để trống")
        @FutureOrPresent(message = "Ngày đặt sân phải là ngày hiện tại hoặc trong tương lai") LocalDate bookingDate,
        @NotNull(message = "Mã khung giờ không được để trống") Long timeSlotId,
        @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự") String note
) {
}
