package project.badminton.booking;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.booking.dto.BookingResponse;
import project.badminton.common.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/manager/bookings")
public class ManagerBookingController {
    private final BookingService bookingService;

    public ManagerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> pendingBookings() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách lịch đặt đang chờ duyệt thành công", bookingService.pendingBookings()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> byDateAndStatus(
            @RequestParam @NotNull(message = "Ngày đặt sân không được để trống")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "CONFIRMED") BookingStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách lịch đặt sân thành công", bookingService.bookingsByDateAndStatus(date, status)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Phê duyệt lịch đặt sân thành công", bookingService.approve(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Từ chối lịch đặt sân thành công", bookingService.reject(id)));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Check-in lịch đặt sân thành công", bookingService.checkIn(id)));
    }
}
