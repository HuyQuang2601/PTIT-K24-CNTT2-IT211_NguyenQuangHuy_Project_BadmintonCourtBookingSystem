package project.badminton.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import project.badminton.auth.dto.AuthResponse;
import project.badminton.auth.dto.ForgotPasswordRequest;
import project.badminton.auth.dto.LoginRequest;
import project.badminton.auth.dto.PasswordResetRequestedResponse;
import project.badminton.auth.dto.RefreshTokenRequest;
import project.badminton.auth.dto.RegisterRequest;
import project.badminton.auth.dto.ResetPasswordRequest;
import project.badminton.user.Role;
import project.badminton.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    @Test
    void registerReturnsCreatedResponse() {
        RegisterRequest request = new RegisterRequest("student", "student@example.com", "password123", "Student", null);
        when(authService.register(request)).thenReturn(userResponse());

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("student", response.getBody().data().username());
    }

    @Test
    void loginReturnsTokenPair() {
        LoginRequest request = new LoginRequest("student", "password123");
        when(authService.login(request)).thenReturn(authResponse());

        var response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access-token", response.getBody().data().accessToken());
    }

    @Test
    void refreshReturnsRotatedTokenPair() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        when(authService.refresh(request)).thenReturn(authResponse());

        var response = controller.refresh(request);

        assertEquals("rotated-refresh-token", response.getBody().data().refreshToken());
    }

    @Test
    void forgotPasswordKeepsGenericMessageAndReturnsDevToken() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("student@example.com");
        when(authService.requestPasswordReset(request.email()))
                .thenReturn(new PasswordResetRequestedResponse("reset-token"));

        var response = controller.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("reset-token", response.getBody().data().resetToken());
    }

    @Test
    void resetPasswordDelegatesToService() {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword123");

        var response = controller.resetPassword(request);

        verify(authService).resetPassword(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().data());
    }

    private AuthResponse authResponse() {
        return new AuthResponse("access-token", "rotated-refresh-token", "Bearer", 1800, Set.of("ROLE_CUSTOMER"));
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "student",
                "student@example.com",
                "Student",
                null,
                Set.of(Role.ROLE_CUSTOMER),
                true,
                LocalDateTime.now()
        );
    }
}
