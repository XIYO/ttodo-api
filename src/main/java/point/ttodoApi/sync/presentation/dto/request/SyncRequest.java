package point.ttodoApi.sync.presentation.dto.request;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class SyncRequest {
    
    @NotNull
    private String clientId;
    
    @NotNull
    private Long lastSyncTimestamp;
    
    @Valid
    @NotNull
    private List<SyncChange> changes;
    
    @Data
    public static class SyncChange {
        @NotNull
        private String operation; // "insert", "update", "delete"
        
        @NotNull
        private String tableName;
        
        @NotNull
        private SyncRecord record;
        
        private Long timestamp;
    }
    
    @Data
    public static class SyncRecord {
        private String id;  // Changed to String for UUID
        private String title;
        private String description;
        private Boolean complete;
        private String createdAt;
        private Long updatedAt;  // Added for Last-Write-Wins
    }
}