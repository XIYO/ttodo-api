package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/repeat-types")
@Tag(name = "반복 타입", description = "투두 반복 타입 관련 API")
public class RepeatTypeController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "반복 타입 목록 조회", 
        description = "Todo 반복 타입 목록을 조회합니다.",
        responses = @ApiResponse(
            responseCode = "200",
            description = "반복 타입 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "반복 타입 목록",
                    value = """
                        {
                          "content": [
                            {
                              "id": 0,
                              "name": "반복 안함"
                            },
                            {
                              "id": 1,
                              "name": "매일"
                            },
                            {
                              "id": 2,
                              "name": "매주"
                            },
                            {
                              "id": 3,
                              "name": "매월"
                            },
                            {
                              "id": 4,
                              "name": "매년"
                            }
                          ]
                        }
                        """
                )
            )
        )
    )
    public Map<String, Object> getRepeatTypes() {
        return Map.of("content", List.of(
            Map.of("id", 0, "name", "반복 안함"),
            Map.of("id", 1, "name", "매일"),
            Map.of("id", 2, "name", "매주"),
            Map.of("id", 3, "name", "매월"),
            Map.of("id", 4, "name", "매년")
        ));
    }
}
