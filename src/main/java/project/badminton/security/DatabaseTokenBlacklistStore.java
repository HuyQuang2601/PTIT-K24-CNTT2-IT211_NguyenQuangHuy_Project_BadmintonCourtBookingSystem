package project.badminton.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.token-blacklist.provider", havingValue = "database", matchIfMissing = true)
public class DatabaseTokenBlacklistStore implements TokenBlacklistStore {
    private final TokenBlacklistRepository repository;

    public DatabaseTokenBlacklistStore(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String tokenHash, Instant expiresAt) {
        if (repository.existsByTokenHash(tokenHash)) {
            return;
        }
        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setTokenHash(tokenHash);
        blacklist.setExpiresAt(expiresAt);
        repository.save(blacklist);
    }

    @Override
    public boolean contains(String tokenHash) {
        return repository.existsByTokenHash(tokenHash);
    }
}
