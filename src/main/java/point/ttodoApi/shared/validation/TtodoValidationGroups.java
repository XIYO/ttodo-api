package point.ttodoApi.shared.validation;

/**
 * TTODO 검증 그룹 정의
 * 도메인별 검증 단계를 위한 그룹 인터페이스
 */
public class TtodoValidationGroups {
    
    /**
     * 생성 시 검증 그룹
     */
    public interface Create { }
    
    /**
     * 수정 시 검증 그룹
     */
    public interface Update { }
    
    /**
     * 삭제 시 검증 그룹
     */
    public interface Delete { }
    
    /**
     * 조회 시 검증 그룹
     */
    public interface Query { }
    
    /**
     * 인증/권한 검증 그룹
     */
    public interface Auth { }
    
    /**
     * 비즈니스 로직 검증 그룹
     */
    public interface Business { }
}