package point.ttodoApi.category.domain;

import lombok.Getter;

/**
 * 협업자 상태
 */
@Getter
public enum CollaboratorStatus {
    /**
     * 초대 대기 중
     */
    PENDING("초대 대기 중"),
    
    /**
     * 초대 수락됨 (활성 협업자)
     */
    ACCEPTED("수락됨"),
    
    /**
     * 초대 거절됨
     */
    REJECTED("거절됨");
    
    private final String description;
    
    CollaboratorStatus(String description) {
        this.description = description;
    }
}