package point.zzicback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.dto.request.CreateTodoRequest;
import point.zzicback.dto.request.UpdateTodoRequest;
import point.zzicback.dto.response.TodoMainResponse;
import point.zzicback.model.Todo;
import point.zzicback.service.TodoService;

import java.util.List;

/**
 * Todo API 컨트롤러
 *
 * <p>To-Do 목록을 조회, 등록, 수정, 삭제하는 API의 엔드포인트를 정의합니다.
 * <p>구현 로직은 실제 DB 연동 또는 Service 레이어에서 담당하게 되며,
 * 이 컨트롤러는 요청/응답에 대한 맵핑만을 담당합니다.
 */
@Tag(name = "Todo API", description = "To-Do 목록을 조회, 등록, 수정, 삭제하는 API")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    private final ObjectMapper objectMapper;

    /**
     * Todo 목록 조회
     *
     * <p>모든 Todo 항목의 목록을 조회합니다.
     * @param isDone Todo를 완료했는지 여부
     * @return Todo 리스트
     */
    @Operation(summary = "Todo 목록 조회", description = "모든 Todo 항목의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 Todo 목록을 조회함",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    // 배열임을 명시하려면 ArraySchema를 사용
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = TodoMainResponse.class)
                                    )
                            )
                    }
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TodoMainResponse> getAll(@Parameter(description = "Todo를 완료했는지 여부") @RequestParam Boolean isDone) {

        List<Todo> todos = this.todoService.getTodoList(isDone);

        // TodoMainResponse로 변환 필요

        return todos.stream()
                .map(todo -> objectMapper.convertValue(todo, TodoMainResponse.class))
                .toList();
    }

    /**
     * 특정 Todo 조회
     *
     * <p>고유 식별자를 이용해 특정 Todo 항목을 조회합니다.
     * @param id 조회할 Todo의 고유 식별자
     * @return 해당 id를 갖는 Todo
     */
    @Operation(summary = "특정 Todo 조회", description = "ID에 해당하는 Todo를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 Todo를 조회함",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Todo.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Todo getTodo(
            @Parameter(description = "조회할 Todo의 ID")
            @PathVariable Long id) {
        return this.todoService.getTodoById(id);
    }

    /**
     * Todo 등록
     *
     * <p>새로운 Todo 항목을 등록합니다.
     * @param createTodoRequest 등록할 Todo 객체
     */
    @Operation(summary = "Todo 등록", description = "새로운 Todo를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 Todo를 생성함",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(
            @Parameter(description = "등록할 Todo 정보")
            @RequestBody @Valid CreateTodoRequest createTodoRequest) {
        Todo todo = objectMapper.convertValue(createTodoRequest, Todo.class);
        this.todoService.createTodo(todo);
    }

    /**
     * Todo 수정
     *
     * <p>기존 Todo 항목의 내용을 수정합니다.
     * @param id 수정할 Todo의 고유 식별자
     * @param updateTodoRequest 수정 내용이 담긴 Todo 객체
     */
    @Operation(summary = "Todo 수정", description = "ID에 해당하는 Todo를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 Todo를 수정함",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modify(
            @Parameter(description = "수정할 Todo의 ID")
            @PathVariable Long id,
            @Parameter(description = "수정할 Todo 정보")
            @RequestBody UpdateTodoRequest updateTodoRequest) {
        Todo todo = objectMapper.convertValue(updateTodoRequest, Todo.class);
        todo.setId(id);
        this.todoService.updateTodo(todo);
    }

    /**
     * Todo 삭제
     *
     * <p>기존에 등록된 Todo 항목을 삭제합니다.
     * @param id 삭제할 Todo의 고유 식별자
     */
    @Operation(summary = "Todo 삭제", description = "ID에 해당하는 Todo를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 Todo를 삭제함",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @Parameter(description = "삭제할 Todo의 ID")
            @PathVariable Long id) {
        this.todoService.deleteTodo(id);
    }
}
