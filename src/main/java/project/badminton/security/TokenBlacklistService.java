package project.badminton.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenBlacklistService {
    private final TokenBlacklistRepository repository;

    public TokenBlacklistService(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    public void blacklist(String token, Instant expiresAt) {
        String hash = hash(token);
        if (repository.existsByTokenHash(hash)) {
            return;
        }
        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setTokenHash(hash);
        blacklist.setExpiresAt(expiresAt);
        repository.save(blacklist);
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByTokenHash(hash(token));
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
