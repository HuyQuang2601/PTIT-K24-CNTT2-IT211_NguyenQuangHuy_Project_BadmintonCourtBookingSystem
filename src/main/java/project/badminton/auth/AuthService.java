package project.badminton.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.badminton.auth.dto.AuthResponse;
import project.badminton.auth.dto.ChangePasswordRequest;
import project.badminton.auth.dto.LoginRequest;
import project.badminton.auth.dto.PasswordResetRequestedResponse;
import project.badminton.auth.dto.RefreshTokenRequest;
import project.badminton.auth.dto.RegisterRequest;
import project.badminton.auth.dto.ResetPasswordRequest;
import project.badminton.common.BusinessException;
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
import project.badminton.user.dto.UserResponse;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final long refreshTokenSeconds;
    private final long passwordResetTokenSeconds;
    private final boolean exposePasswordResetToken;

    public AuthService(
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenBlacklistService tokenBlacklistService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            @Value("${app.jwt.refresh-token-seconds}") long refreshTokenSeconds,
            @Value("${app.password-reset.token-seconds:900}") long passwordResetTokenSeconds,
            @Value("${app.password-reset.expose-token:false}") boolean exposePasswordResetToken
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.passwordResetTokenSeconds = passwordResetTokenSeconds;
        this.exposePasswordResetToken = exposePasswordResetToken;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRoles(Set.of(Role.ROLE_CUSTOMER));
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user).getToken();
        return authResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.findByUserAndRevokedFalse(refreshToken.getUser())
                    .forEach(activeToken -> activeToken.setRevoked(true));
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn hoặc bị thu hồi");
        }
        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        RefreshToken rotatedToken = createRefreshToken(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return authResponse(jwtService.generateAccessToken(userDetails), rotatedToken.getToken(), user);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        refreshTokenRepository.findByUserAndRevokedFalse(user).forEach(refreshToken -> refreshToken.setRevoked(true));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.findByUserAndRevokedFalse(user)
                .forEach(refreshToken -> refreshToken.setRevoked(true));
    }

    @Transactional
    public PasswordResetRequestedResponse requestPasswordReset(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    passwordResetTokenRepository.deleteByUser(user);
                    String rawToken = UUID.randomUUID().toString();
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setTokenHash(hash(rawToken));
                    resetToken.setUser(user);
                    resetToken.setExpiresAt(Instant.now().plusSeconds(passwordResetTokenSeconds));
                    passwordResetTokenRepository.save(resetToken);
                    return new PasswordResetRequestedResponse(exposePasswordResetToken ? rawToken : null);
                })
                .orElseGet(() -> new PasswordResetRequestedResponse(null));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));
        if (resetToken.getUsedAt() != null || !resetToken.getExpiresAt().isAfter(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(Instant.now());
        refreshTokenRepository.findByUserAndRevokedFalse(user)
                .forEach(refreshToken -> refreshToken.setRevoked(true));
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenSeconds));
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse authResponse(String accessToken, String refreshToken, User user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenSeconds(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Thiếu Bearer token");
        }
        return authorizationHeader.substring(7);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRoles(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Thuật toán SHA-256 không khả dụng", ex);
        }
    }
}
