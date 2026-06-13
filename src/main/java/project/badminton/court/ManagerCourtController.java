package project.badminton.court;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.common.ApiResponse;
import project.badminton.court.dto.CourtRequest;
import project.badminton.court.dto.CourtResponse;

@RestController
@RequestMapping("/api/v1/manager/courts")
public class ManagerCourtController {
    private final CourtService courtService;

    public ManagerCourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourtResponse>>> search(@RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách sân cầu lông thành công", courtService.search(keyword, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourtResponse>> create(Authentication authentication, @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo sân cầu lông thành công", courtService.create(authentication.getName(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> update(@PathVariable Long id, @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật sân cầu lông thành công", courtService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courtService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
