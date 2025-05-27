package point.zzicback.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    public void save(UUID memberId, String refreshToken, String accessToken) {
        String refreshKey = "refresh:" + memberId;
        String accessKey = "access:" + memberId;

        redisTemplate.opsForValue().set(refreshKey, refreshToken, 365, TimeUnit.DAYS); // 리프레시 토큰 1년
        redisTemplate.opsForValue().set(accessKey, accessToken, 5, TimeUnit.MINUTES);  // 액세스 토큰 5분
    }

    public void delete(UUID memberId) {
        redisTemplate.delete(buildKey(memberId));
    }

    private String buildKey(UUID memberId) {
        return "refresh:" + memberId;
    }

    private String buildAccessKey(UUID memberId) {
        return "access:" + memberId.toString();
    }

    public void updateAccessToken(UUID memberId, String newAccessToken) {
        String accessKey = buildAccessKey(memberId);
        redisTemplate.opsForValue().set(accessKey, newAccessToken, 5, TimeUnit.MINUTES); // TTL = 5분
    }

    public String getRefreshToken(UUID memberId) {
        String key = "refresh:" + memberId;
        return redisTemplate.opsForValue().get(key);
    }
}