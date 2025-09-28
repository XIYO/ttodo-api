package point.ttodoApi.user.infrastructure;

import org.springframework.stereotype.Component;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.specification.BaseSpecification;

import java.util.Set;

/**
 * User 엔티티를 위한 동적 쿼리 Specification
 */
@Component
public class UserSpecification extends BaseSpecification<User> {

  // User 엔티티에서 정렬 가능한 필드들
  private static final Set<String> MEMBER_SORT_FIELDS = Set.of(
          "email",
          "nickname",
          "introduction",
          "role",
          "lastLoginAt"
  );

  @Override
  protected Set<String> getAllowedSortFields() {
    // 공통 필드와 User 특화 필드를 합침
    Set<String> allowedFields = new java.util.HashSet<>(COMMON_SORT_FIELDS);
    allowedFields.addAll(MEMBER_SORT_FIELDS);
    return allowedFields;
  }
}