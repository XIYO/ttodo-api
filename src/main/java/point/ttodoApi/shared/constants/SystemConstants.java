package point.ttodoApi.shared.constants;

import java.util.UUID;

/**
 * 시스템 전역 상수 정의
 */
public final class SystemConstants {
    
    private SystemConstants() {
        // 인스턴스화 방지
    }
    
    /**
     * 사전 정의된 시스템 사용자
     */
    public static final class SystemUsers {
        // 익명 사용자
        public static final UUID ANON_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        public static final String ANON_USER_EMAIL = "anon@ttodo.dev";
        public static final String ANON_USER_NICKNAME = "익명사용자";
        public static final String ANON_USER_PASSWORD = "password123";
        
        // 루트 사용자
        public static final UUID ROOT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        public static final String ROOT_USER_EMAIL = "root@ttodo.dev";
        public static final String ROOT_USER_NICKNAME = "루트관리자";
        public static final String ROOT_USER_PASSWORD = "rootpassword123";
        
        private SystemUsers() {
            // 인스턴스화 방지
        }
    }
}