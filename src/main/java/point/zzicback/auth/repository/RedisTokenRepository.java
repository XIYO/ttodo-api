package point.zzicback.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Profile("redis")
public class RedisTokenRepository implements TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String key, String value, long expirationSeconds) {
        redisTemplate.opsForValue().set(key, value, expirationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }
}
