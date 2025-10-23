package com.sisinnov.pms.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "refresh_token:";
    private static final Duration TTL = Duration.ofDays(7);

    public void save(String token, UUID userId) {
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), TTL);
    }

    public Optional<UUID> findUserIdByToken(String token) {
        String key = KEY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);

        if (userId == null) {
            return Optional.empty();
        }

        return Optional.of(UUID.fromString(userId));
    }

    public void deleteByToken(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.delete(key);
    }

    public void deleteAllByUserId(UUID userId) {
        redisTemplate.keys(KEY_PREFIX + "*")
                .forEach(key -> {
                    String storedUserId = redisTemplate.opsForValue().get(key);
                    if (userId.toString().equals(storedUserId)) {
                        redisTemplate.delete(key);
                    }
                });
    }
}