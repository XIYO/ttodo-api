package point.ttodoApi.member.infrastructure;

import org.springframework.stereotype.Component;
import point.ttodoApi.shared.specification.BaseSpecification;
import point.ttodoApi.member.domain.Member;

import java.util.Set;

/**
 * Member 엔티티를 위한 동적 쿼리 Specification
 */
@Component
public class MemberSpecification extends BaseSpecification<Member> {
    
    // Member 엔티티에서 정렬 가능한 필드들
    private static final Set<String> MEMBER_SORT_FIELDS = Set.of(
        "email",
        "nickname",
        "introduction",
        "role",
        "lastLoginAt"
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        // 공통 필드와 Member 특화 필드를 합침
        Set<String> allowedFields = new java.util.HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(MEMBER_SORT_FIELDS);
        return allowedFields;
    }
}