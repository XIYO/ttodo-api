package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.profile.application.query.ProfileQuery;
import point.ttodoApi.profile.domain.Profile;

import jakarta.validation.Valid;

/**
 * Profile Query Service
 * TTODO 아키텍처 패턴: Query 처리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ProfileQueryService {
    
    private final ProfileService profileService; // 기존 서비스 위임

    /**
     * 프로필 조회
     */
    public Profile getProfile(@Valid ProfileQuery query) {
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        return profileService.getProfile(query.userId());
    }
}