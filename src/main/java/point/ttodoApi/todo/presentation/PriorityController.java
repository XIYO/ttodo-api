package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "우선순위(Priority) 참조 데이터", description = "할 일의 우선순위 레벨을 정의하는 참조 데이터 API입니다. 낮음(0), 보통(1), 높음(2)의 세 가지 우선순위를 제공합니다.")
@RestController
@RequestMapping("/priorities")
public class PriorityController {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(
          summary = "우선순위 참조 데이터 조회",
          description = "할 일에 설정할 수 있는 우선순위 목록을 조회합니다. 이 데이터는 할 일 생성/수정 시 우선순위 선택을 위한 고정된 참조 데이터입니다.",
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
