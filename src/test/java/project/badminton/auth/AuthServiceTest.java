package project.badminton.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.badminton.auth.dto.ChangePasswordRequest;
import project.badminton.auth.dto.PasswordResetRequestedResponse;
import project.badminton.auth.dto.RefreshTokenRequest;
import project.badminton.auth.dto.RegisterRequest;
import project.badminton.auth.dto.ResetPasswordRequest;
import project.badminton.security.CustomUserDetailsService;
import project.badminton.security.JwtService;
import project.badminton.security.PasswordResetToken;
import project.badminton.security.PasswordResetTokenRepository;
import project.badminton.security.RefreshToken;
import project.badminton.security.RefreshTokenRepository;
import project.badminton.security.TokenBlacklistService;
import project.badminton.user.Role;
import project.badminton.user.User;
import project.badminton.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserDetails userDetails;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                userDetailsService,
                jwtService,
                refreshTokenRepository,
                userRepository,
                passwordEncoder,
                tokenBlacklistService,
                passwordResetTokenRepository,
                604800,
                900,
                true
        );
    }

    @Test
    void registerCreatesEnabledCustomerWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("student", "student@example.com", "password123", "Student", "0900000000");
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(request);

        assertEquals("student", response.username());
        assertEquals(Set.of(Role.ROLE_CUSTOMER), response.roles());
        assertTrue(response.enabled());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void refreshRevokesOldTokenAndReturnsRotatedToken() {
        User user = customer();
        RefreshToken oldToken = refreshToken("old-token", user);
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername("customer")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenSeconds()).thenReturn(1800L);

        var response = authService.refresh(new RefreshTokenRequest("old-token"));

        assertTrue(oldToken.isRevoked());
        assertNotEquals("old-token", response.refreshToken());
        assertEquals("new-access-token", response.accessToken());
    }

    @Test
    void forgotPasswordCreatesExpiringHashedToken() {
        User user = customer();
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetRequestedResponse response = authService.requestPasswordReset("customer@example.com");

        assertNotNull(response.resetToken());
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordConsumesTokenAndRevokesRefreshTokens() {
        User user = customer();
        RefreshToken refreshToken = refreshToken("active-token", user);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().plusSeconds(300));
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");
        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(List.of(refreshToken));

        authService.resetPassword(new ResetPasswordRequest("raw-token", "newPassword123"));

        assertEquals("new-encoded-password", user.getPassword());
        assertNotNull(resetToken.getUsedAt());
        assertTrue(refreshToken.isRevoked());
    }

    @Test
    void changePasswordValidatesCurrentPasswordAndRevokesSessions() {
        User user = customer();
        user.setPassword("old-encoded-password");
        RefreshToken refreshToken = refreshToken("active-token", user);
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "old-encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");
        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(List.of(refreshToken));

        authService.changePassword("customer", new ChangePasswordRequest("password123", "newPassword123"));

        assertEquals("new-encoded-password", user.getPassword());
        assertTrue(refreshToken.isRevoked());
    }

    private User customer() {
        User user = new User();
        user.setUsername("customer");
        user.setEmail("customer@example.com");
        user.setFullName("Customer");
        user.setRoles(Set.of(Role.ROLE_CUSTOMER));
        user.setEnabled(true);
        return user;
    }

    private RefreshToken refreshToken(String token, User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(300));
        return refreshToken;
    }
}
