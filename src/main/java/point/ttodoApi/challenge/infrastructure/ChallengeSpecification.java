package point.ttodoApi.challenge.infrastructure;

import org.springframework.stereotype.Component;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.shared.specification.BaseSpecification;

import java.util.Set;

/**
 * Challenge 엔티티를 위한 동적 쿼리 Specification
 */
@Component  
public class ChallengeSpecification extends BaseSpecification<Challenge> {
    
    // Challenge 엔티티에서 정렬 가능한 필드들
    private static final Set<String> CHALLENGE_SORT_FIELDS = Set.of(
        "title",
        "description",
        "startDate",
        "endDate",
        "periodType",
        "visibility",
        "maxParticipants",
        "creatorId"
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        // 공통 필드와 Challenge 특화 필드를 합침
        Set<String> allowedFields = new java.util.HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(CHALLENGE_SORT_FIELDS);
        return allowedFields;
    }
}