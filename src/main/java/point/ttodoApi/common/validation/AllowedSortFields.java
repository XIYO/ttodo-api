package point.ttodoApi.common.validation;

import java.util.Set;

/**
 * 도메인별 허용된 정렬 필드 정의
 */
public final class AllowedSortFields {
    
    private AllowedSortFields() {
        // 유틸리티 클래스
    }
    
    /**
     * 공통 정렬 필드 (모든 엔티티에서 사용 가능)
     */
    public static final Set<String> COMMON_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt"
    );
    
    /**
     * Todo 엔티티 정렬 필드
     */
    public static final Set<String> TODO_FIELDS = Set.of(
        "id",
        "createdAt", 
        "updatedAt",
        "title",
        "complete",
        "date",
        "priorityId",
        "orderIndex",
        "owner.nickname",
        "category.title"
    );
    
    /**
     * Category 엔티티 정렬 필드
     */
    public static final Set<String> CATEGORY_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt",
        "title",
        "colorCode",
        "orderIndex",
        "owner.nickname"
    );
    
    /**
     * Member 엔티티 정렬 필드
     */
    public static final Set<String> MEMBER_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt",
        "email",
        "nickname",
        "role",
        "lastLoginAt"
    );
    
    /**
     * Challenge 엔티티 정렬 필드
     */
    public static final Set<String> CHALLENGE_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt",
        "title",
        "startDate",
        "endDate",
        "periodType",
        "visibility",
        "maxParticipants",
        "creatorId"
    );
    
    /**
     * ChallengeTodo 엔티티 정렬 필드
     */
    public static final Set<String> CHALLENGE_TODO_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt",
        "challengeTitle",
        "completed",
        "completedAt"
    );
}