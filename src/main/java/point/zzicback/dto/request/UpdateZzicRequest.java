package point.zzicback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import point.zzicback.domain.Zzic;

@Data
@Schema(description = "To-Do 업데이트 요청")
public class UpdateZzicRequest {
    @Schema(description = "To-Do 항목의 제목", example = "장보기 수정")
    private String title;

    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란, 치즈 구입")
    private String description;

    @Schema(description = "To-Do 항목의 완료 여부", example = "false")
    private Boolean done;

    /**
     * Request DTO → Entity 변환 메서드
     */
    public Zzic toEntity(Long id) {
        Zzic zzic = new Zzic();
        zzic.setId(id);
        zzic.setTitle(this.title);
        zzic.setDescription(this.description);
        zzic.setDone(this.done);
        return zzic;
    }
}
