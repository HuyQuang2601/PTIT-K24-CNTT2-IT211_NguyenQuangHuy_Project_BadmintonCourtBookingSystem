package project.badminton.timeslot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.common.ApiResponse;
import project.badminton.timeslot.dto.TimeSlotResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/time-slots")
public class PublicTimeSlotController {
    private final TimeSlotService timeSlotService;

    public PublicTimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> activeTimeSlots() {
        return ResponseEntity.ok(ApiResponse.ok("Time slots retrieved successfully", timeSlotService.activeTimeSlots()));
    }
}
