package point.zzicback.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테스트 응답")
public record TestResponse(
    @Schema(description = "ID", example = "1")
    Long id,
    
    @Schema(description = "메시지", example = "테스트 메시지")
    String message,
    
    @Schema(description = "요청된 더미1", example = "더미값1")
    String receivedDummy1,
    
    @Schema(description = "요청된 더미2", example = "더미값2")
    String receivedDummy2,
    
    @Schema(description = "요청된 더미 숫자", example = "123")
    Integer receivedDummyNumber,
    
    @Schema(description = "요청된 더미 불린", example = "true")
    Boolean receivedDummyBoolean
) {
}
