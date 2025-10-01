package point.ttodoApi.sync.presentation.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class SyncResponse {
    boolean success;
    Long serverTimestamp;
    List<SyncResult> results;
    List<ConflictRecord> conflicts;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResult {
        private String localId;
        private Integer serverId;
        private String operation;
        private boolean success;
        private String error;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictRecord {
        private Integer id;
        private Object serverVersion;
        private Object clientVersion;
        private String resolution;
    }
}