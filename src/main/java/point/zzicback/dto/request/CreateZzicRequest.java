package point.zzicback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import point.zzicback.domain.Zzic;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "To-Do 생성 요청", requiredProperties = {"title"})
public class CreateZzicRequest {
    @NotBlank
    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    private String title;

    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입")
    private String description;

    /**
     * Request DTO → Entity 변환 메서드
     */
    public Zzic toEntity() {
        Zzic zzic = new Zzic();
        zzic.setTitle(this.title);
        zzic.setDescription(this.description);
        zzic.setDone(false);
        return zzic;
    }
}
