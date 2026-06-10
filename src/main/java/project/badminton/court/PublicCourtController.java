package project.badminton.court;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.common.ApiResponse;
import project.badminton.court.dto.CourtResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
public class PublicCourtController {
    private final CourtService courtService;

    public PublicCourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourtResponse>>> activeCourts() {
        return ResponseEntity.ok(ApiResponse.ok("Courts retrieved successfully", courtService.activeCourts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Court retrieved successfully", courtService.get(id)));
    }
}
