package project.badminton.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.booking.dto.BookingCreateRequest;
import project.badminton.booking.dto.BookingResponse;
import project.badminton.common.ApiResponse;
import project.badminton.court.dto.CourtResponse;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/customer/bookings")
public class CustomerBookingController {
    private final BookingService bookingService;

    public CustomerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(Authentication authentication, @Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đặt sân thành công", bookingService.createBooking(authentication.getName(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> history(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử đặt sân thành công", bookingService.customerHistory(authentication.getName())));
    }

    @GetMapping("/available-courts")
    public ResponseEntity<ApiResponse<List<CourtResponse>>> availableCourts(
            @RequestParam @NotNull(message = "Ngày cần kiểm tra không được để trống")
            @FutureOrPresent(message = "Ngày cần kiểm tra phải là ngày hiện tại hoặc trong tương lai") LocalDate date,
            @RequestParam @NotNull(message = "Mã khung giờ không được để trống") Long timeSlotId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách sân còn trống thành công", bookingService.availableCourts(date, timeSlotId)));
    }
}
