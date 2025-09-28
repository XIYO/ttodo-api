package point.ttodoApi.experience.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.experience.application.query.UserLevelQuery;
import point.ttodoApi.experience.application.result.UserLevelResult;
import point.ttodoApi.experience.application.ExperienceService;

import jakarta.validation.Valid;

/**
 * Experience Query Service
 * TTODO 아키텍처 패턴: Query 처리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ExperienceQueryService {
    
    private final ExperienceService experienceService; // 기존 서비스 위임

    /**
     * 회원 레벨 정보 조회
     */
    public UserLevelResult getUserLevel(@Valid UserLevelQuery query) {
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        return experienceService.getUserLevel(query.userId());
    }
}