package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import point.ttodoApi.todo.domain.recurrence.EndConditionType;

import java.time.LocalDate;

@Schema(description = "반복 종료 조건")
public record EndConditionRequest(
        @Schema(description = "종료 유형", example = "UNTIL", allowableValues = {"NEVER","UNTIL","COUNT"})
        EndConditionType type,
        @Schema(description = "종료일(UNTIL일 때)", example = "2025-12-31")
        LocalDate until,
        @Schema(description = "횟수(COUNT일 때)", example = "10")
        Integer count
) {}

