package project.badminton.security;

import java.time.Instant;

public interface TokenBlacklistStore {
    void save(String tokenHash, Instant expiresAt);

    boolean contains(String tokenHash);
}
