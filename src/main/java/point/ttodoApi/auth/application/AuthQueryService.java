package point.ttodoApi.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.auth.application.query.DevTokenQuery;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.application.UserQueryService;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.error.BusinessException;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Auth Query Service
 * TTODO 아키텍처 패턴: Query(읽기) 처리 전용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class AuthQueryService {
    
    private static final String USER_ROLE = "ROLE_USER";
    private static final String ANON_EMAIL_FALLBACK = "anon@ttodo.dev";
    
    private final UserService UserService; // 기존 호환성 유지
    private final UserQueryService UserQueryService; // TTODO: Query 처리
    private final ProfileService profileService;
    private final TokenService tokenService;
    
    /**
     * 개발용 토큰 발급 (100년 만료)
     */
    public AuthResult getDevToken(@Valid DevTokenQuery query) {
        log.debug("Processing dev token request for device: {}", query.deviceId());
        
        // TTODO 아키텍처 패턴: Query 서비스 사용
        User user = UserQueryService.findUserEntityByEmail(ANON_EMAIL_FALLBACK)
            .orElseThrow(() -> new BusinessException("익명 사용자를 찾을 수 없습니다."));
            
        Profile profile = profileService.getProfile(user.getId());
        
        // 100년 만료 토큰 발급 (개발용) - Profile.nickname 사용
        TokenService.TokenResult tokenResult = tokenService.createLongLivedTokenPair(
            user.getId().toString(),
            query.deviceId(),
            user.getEmail(),
            "temp_nickname",  // TokenService에서 Profile에서 가져옴
            List.of(new SimpleGrantedAuthority(USER_ROLE))
        );
        
        log.debug("Dev token issued for anonymous user: {}", user.getId());
        
        return new AuthResult(
            tokenResult.accessToken(),
            tokenResult.refreshToken(),
            tokenResult.deviceId(),
            user.getId(),
            user.getEmail(),
            profile.getNickname()  // Profile.nickname 사용
        );
    }
}