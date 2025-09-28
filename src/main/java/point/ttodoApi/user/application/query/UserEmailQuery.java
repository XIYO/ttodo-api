package point.ttodoApi.user.application.query;

import point.ttodoApi.user.domain.validation.required.ValidUserEmail;

/**
 * 이메일로 회원 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 이메일 기반 회원 조회 요청 캡슐화
 */
public record UserEmailQuery(
        @ValidUserEmail
        String email
) {
}