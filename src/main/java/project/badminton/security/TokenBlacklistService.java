package project.badminton.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenBlacklistService {
    private final TokenBlacklistStore store;

    public TokenBlacklistService(TokenBlacklistStore store) {
        this.store = store;
    }

    public void blacklist(String token, Instant expiresAt) {
        String hash = hash(token);
        store.save(hash, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        return store.contains(hash(token));
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Thuật toán SHA-256 không khả dụng", ex);
        }
    }
}
