package point.ttodoApi.experience.application.command;

import point.ttodoApi.experience.domain.validation.required.ValidExperienceAmount;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 경험치 추가 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 경험치 추가 요청 캡슐화
 */
public record AddExperienceCommand(
        @ValidUserId
        UUID userId,
        
        @ValidExperienceAmount
        int amount
) {
}