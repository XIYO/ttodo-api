package point.zzicback.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Todo
 *
 * <p>To-Do 항목을 표현하는 엔티티 클래스입니다.
 * <p>직렬화(Serializable) 가능하도록 선언하였으며,
 * Lombok의 @Data로 게터/세터, equals, hashCode, toString 메서드를 자동 생성합니다.
 */
@Data
@Schema(description = "To-Do 항목을 표현하는 모델")
public class Todo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * To-Do 항목의 고유 식별자
     */
    @Schema(description = "To-Do 항목의 고유 식별자", example = "1", required = true)
    private Integer id;

    /**
     * To-Do 항목의 제목
     */
    @Schema(description = "To-Do 항목의 제목", example = "장보기", required = true)
    private String title;

    /**
     * To-Do 항목의 상세 설명
     */
    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입", required = false)
    private String description;

    /**
     * To-Do 항목 완료 여부
     */
    @Schema(description = "To-Do 항목의 완료 여부", example = "false", required = true)
    private Boolean done;
}
