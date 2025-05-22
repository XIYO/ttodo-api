package point.zzicback.todo.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import point.zzicback.member.domain.Member;

import java.io.Serializable;

/**
 * TO-DO
 *
 * <p>To-Do 항목을 표현하는 엔티티 클래스입니다.
 * <p>직렬화(Serializable) 가능하도록 선언하였으며,
 * Lombok의 @Data로 게터/세터, equals, hashCode, toString 메서드를 자동 생성합니다.
 */
@Schema(description = "TO-DO 항목을 표현하는 모델")
@Entity
@Getter
@NoArgsConstructor
@Setter
public class Todo implements Serializable {

    /**
     * TO-DO 항목의 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TO-DO 항목의 제목
     */
    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    @Column(nullable = false)
    private String title;

    /**
     * TO-DO 항목의 상세 설명
     */
    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입")
    private String description;

    /**
     * TO-DO 항목 완료 여부
     */
    @Schema(description = "To-Do 항목의 완료 여부", example = "false")
    @Column(nullable = false)
    private Boolean done = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
