package point.ttodoApi.common.validation;

/**
 * 도메인별 정렬 필드 제공자
 */
public enum SortFieldsProvider {
    /**
     * Todo 엔티티 정렬 필드
     */
    TODO,
    
    /**
     * Category 엔티티 정렬 필드
     */
    CATEGORY,
    
    /**
     * Member 엔티티 정렬 필드
     */
    MEMBER,
    
    /**
     * Challenge 엔티티 정렬 필드
     */
    CHALLENGE,
    
    /**
     * ChallengeTodo 엔티티 정렬 필드
     */
    CHALLENGE_TODO,
    
    /**
     * 공통 필드만 허용 (id, createdAt, updatedAt)
     */
    COMMON,
    
    /**
     * 정렬 허용 안 함
     */
    NONE
}