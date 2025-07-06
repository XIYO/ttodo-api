package point.zzicback.challenge.domain;

/**
 * 챌린지 가시성 타입
 */
public enum ChallengeVisibility {
    PUBLIC("공개"),           // 검색/목록에 노출
    INVITE_ONLY("초대 전용");  // 링크로만 접근
    
    private final String description;
    
    ChallengeVisibility(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}