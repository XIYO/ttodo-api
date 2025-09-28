package point.ttodoApi.sync.presentation.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SyncResponse {
    private boolean success;
    private Long serverTimestamp;
    private List<SyncResult> results;
    private List<ConflictRecord> conflicts;
    
    @Data
    @Builder
    public static class SyncResult {
        private String localId;
        private Integer serverId;
        private String operation;
        private boolean success;
        private String error;
    }
    
    @Data
    @Builder
    public static class ConflictRecord {
        private Integer id;
        private Object serverVersion;
        private Object clientVersion;
        private String resolution; // "server_wins", "client_wins", "merged"
    }
}