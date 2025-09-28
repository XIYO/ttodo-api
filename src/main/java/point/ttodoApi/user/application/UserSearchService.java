package point.ttodoApi.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.user.domain.*;
import point.ttodoApi.user.infrastructure.UserSpecification;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.user.presentation.dto.request.UserSearchRequest;
import point.ttodoApi.shared.specification.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 동적 쿼리를 사용한 User 검색 서비스 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

  private final UserRepository UserRepository;
  private final UserSpecification UserSpecification;
  private final SortValidator sortValidator;

  /**
   * 멤버 검색 - 다양한 조건으로 검색
   */
  public Page<User> searchUsers(UserSearchRequest request, Pageable pageable) {
    // 정렬 필드 검증
    sortValidator.validateSort(pageable.getSort(), UserSpecification);

    // SpecificationBuilder를 사용한 동적 쿼리 구성
    SpecificationBuilder<User> builder = new SpecificationBuilder<>(UserSpecification);

    Specification<User> spec = builder
            // 기본 조건 - 활성 사용자만
            .with("active", true)

            // 선택적 조건들
            .withLike("email", request.getEmailKeyword())
            .withLike("nickname", request.getNicknameKeyword())
            .with("role", request.getRole())
            .withDateRange("lastLoginAt", request.getLastLoginFrom(), request.getLastLoginTo())

            // 복잡한 조건 예제 - 최근 활동 사용자
            .withIf(request.isRecentlyActiveOnly(), builder2 ->
                    builder2.withBetween("lastLoginAt",
                            LocalDateTime.now().minusDays(30),
                            LocalDateTime.now()))

            .build();

    return UserRepository.findAll(spec, pageable);
  }

  /**
   * 활성 관리자 목록 조회
   */
  public List<User> getActiveAdmins() {
    SpecificationBuilder<User> builder = new SpecificationBuilder<>(UserSpecification);

    Specification<User> spec = builder
            .with("active", true)
            .with("role", Role.ADMIN)
            .build();

    return UserRepository.findAll(spec);
  }

  /**
   * 오랜 기간 로그인하지 않은 사용자 조회
   */
  public Page<User> getInactiveUsers(int days, Pageable pageable) {
    SpecificationBuilder<User> builder = new SpecificationBuilder<>(UserSpecification);

    Specification<User> spec = builder
            .with("active", true)
            .lessThan("lastLoginAt", LocalDateTime.now().minusDays(days))
            .build();

    return UserRepository.findAll(spec, pageable);
  }

}