package project.badminton.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.token-blacklist.provider", havingValue = "redis")
public class RedisTokenBlacklistStore implements TokenBlacklistStore {
    private static final String KEY_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String tokenHash, Instant expiresAt) {
        Duration timeToLive = Duration.between(Instant.now(), expiresAt);
        if (!timeToLive.isNegative() && !timeToLive.isZero()) {
            redisTemplate.opsForValue().set(KEY_PREFIX + tokenHash, "revoked", timeToLive);
        }
    }

    @Override
    public boolean contains(String tokenHash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenHash));
    }
}
