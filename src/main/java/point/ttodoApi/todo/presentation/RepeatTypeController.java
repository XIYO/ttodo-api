package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/repeat-types")
@Tag(name = "반복 타입(RepeatType) 참조 데이터", description = "할 일의 반복 스케줄을 설정하기 위한 참조 데이터 API입니다. 반복 안함(0), 매일(1), 매주(2), 매월(3), 매년(4)의 다섯 가지 반복 타입을 제공합니다.")
public class RepeatTypeController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "반복 타입 참조 데이터 조회", 
        description = "할 일에 설정할 수 있는 반복 타입 목록을 조회합니다. 이 데이터는 할 일 생성/수정 시 반복 스케줄 선택을 위한 고정된 참조 데이터입니다.\n\n" +
                      "반복 타입 설명:\n" +
                      "- 반복 안함: 일회성 할 일\n" +
                      "- 매일: 매일 반복\n" +
                      "- 매주: 매주 같은 요일에 반복\n" +
                      "- 매월: 매월 같은 날짜에 반복\n" +
                      "- 매년: 매년 같은 날짜에 반복",
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
