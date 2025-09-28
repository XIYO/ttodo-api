package point.ttodoApi.user.application.query;

import org.springframework.data.domain.Pageable;

/**
 * 회원 목록 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 회원 목록 조회 요청 캡슐화
 */
public record UserListQuery(
        Pageable pageable
) {
}