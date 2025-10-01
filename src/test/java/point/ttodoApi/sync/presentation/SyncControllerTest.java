package point.ttodoApi.sync.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.sync.application.SyncService;
import point.ttodoApi.sync.presentation.dto.request.SyncRequest;
import point.ttodoApi.sync.presentation.dto.response.SyncResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("SyncController 단위 테스트")
@Tag("unit")
@Tag("sync")
@Tag("controller")
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SyncService syncService;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("1. POST /sync/simple-todo")
    class SyncEndpoint {

        @Test
        @DisplayName("동기화 요청 성공")
        void syncSimpleTodos_Success() throws Exception {
            SyncResponse response = SyncResponse.builder()
                .success(true)
                .serverTimestamp(1234L)
                .results(Collections.emptyList())
                .conflicts(Collections.emptyList())
                .build();
            given(syncService.processSync(any(SyncRequest.class))).willReturn(response);

            SyncRequest.SyncRecord record = new SyncRequest.SyncRecord();
            record.setId("1");
            record.setTitle("Test");
            record.setDescription("Desc");
            record.setComplete(Boolean.FALSE);
            record.setCreatedAt("2024-01-01T00:00:00Z");
            record.setUpdatedAt(1234L);

            SyncRequest.SyncChange change = new SyncRequest.SyncChange();
            change.setOperation("insert");
            change.setTableName("simple_todo");
            change.setRecord(record);
            change.setTimestamp(1234L);

            SyncRequest request = new SyncRequest();
            request.setClientId("client-1");
            request.setLastSyncTimestamp(0L);
            request.setChanges(List.of(change));

            mockMvc.perform(post("/sync/simple-todo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(syncService).processSync(any(SyncRequest.class));
        }
    }

    @Nested
    @DisplayName("2. GET /sync/simple-todos")
    class QueryEndpoint {

        @Test
        @DisplayName("심플 TODO 목록 조회 성공")
        void getSimpleTodos_Success() throws Exception {
            given(jdbcTemplate.queryForList(anyString())).willReturn(List.of(Map.of("id", "1")));

            mockMvc.perform(get("/sync/simple-todos"))
                .andExpect(status().isOk());

            verify(jdbcTemplate).queryForList(anyString());
        }
    }
}
