package project.badminton.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import project.badminton.user.dto.UserRequest;
import project.badminton.user.dto.UserResponse;
import project.badminton.user.dto.UserUpdateRequest;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> search(@RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách người dùng thành công", userService.search(keyword, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin người dùng thành công", userService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo người dùng thành công", userService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật người dùng thành công", userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
