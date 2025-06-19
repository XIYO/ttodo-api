package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/priorities")
public class PriorityController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "우선순위 목록 조회", 
        description = "Todo 우선순위 목록을 조회합니다.",
        responses = @ApiResponse(
            responseCode = "200",
            description = "우선순위 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "우선순위 목록",
                    value = """
                        {
                          "content": [
                            {
                              "id": 0,
                              "name": "낮음"
                            },
                            {
                              "id": 1,
                              "name": "보통"
                            },
                            {
                              "id": 2,
                              "name": "높음"
                            }
                          ]
                        }
                        """
                )
            )
        )
    )
    public Map<String, Object> getPriorities() {
        return Map.of("content", List.of(
            Map.of("id", 0, "name", "낮음"),
            Map.of("id", 1, "name", "보통"),
            Map.of("id", 2, "name", "높음")
        ));
    }
}
