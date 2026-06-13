package project.badminton.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.badminton.auth.dto.AuthResponse;
import project.badminton.auth.dto.ChangePasswordRequest;
import project.badminton.auth.dto.ForgotPasswordRequest;
import project.badminton.auth.dto.LoginRequest;
import project.badminton.auth.dto.PasswordResetRequestedResponse;
import project.badminton.auth.dto.RefreshTokenRequest;
import project.badminton.auth.dto.RegisterRequest;
import project.badminton.auth.dto.ResetPasswordRequest;
import project.badminton.common.ApiResponse;
import project.badminton.user.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký tài khoản thành công", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Làm mới token thành công", authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResetRequestedResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi",
                authService.requestPasswordReset(request.email())
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Đặt lại mật khẩu thành công", null));
    }
}
