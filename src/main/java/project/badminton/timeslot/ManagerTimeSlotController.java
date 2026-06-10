package project.badminton.timeslot;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.common.ApiResponse;
import project.badminton.timeslot.dto.TimeSlotRequest;
import project.badminton.timeslot.dto.TimeSlotResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/time-slots")
public class ManagerTimeSlotController {
    private final TimeSlotService timeSlotService;

    public ManagerTimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> all() {
        return ResponseEntity.ok(ApiResponse.ok("Time slots retrieved successfully", timeSlotService.all()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotResponse>> create(@Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Time slot created successfully", timeSlotService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> update(@PathVariable Long id, @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Time slot updated successfully", timeSlotService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
