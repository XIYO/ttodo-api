package point.zzicback.todo.presentation;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.common.security.etc.MemberPrincipal;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.domain.mapper.TodoMapper;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.dto.request.CreateTodoRequest;
import point.zzicback.todo.domain.dto.request.UpdateTodoRequest;
import point.zzicback.todo.domain.dto.response.TodoMainResponse;

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
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final TodoMapper todoMapper;

    /**
     * Todo 목록 조회
     *
     * <p>모든 Todo 항목의 목록을 조회합니다.
     *
     * @param done Todo를 완료했는지 여부
     * @return Todo 리스트
     */
    @Operation(summary = "Todo 목록 조회", description = "모든 Todo 항목의 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 Todo 목록을 조회함", content = {@Content(mediaType = "application/json",
            // 배열임을 명시하려면 ArraySchema를 사용
            array = @ArraySchema(schema = @Schema(implementation = TodoMainResponse.class)))})})
    @GetMapping("/me/todos")
    @ResponseStatus(HttpStatus.OK)
    public List<TodoMainResponse> getAll(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(
                    description = "Todo 완료 여부 필터: true = 완료, false = 미완료, 비워두면 전체 조회",
                    example = "true",
                    required = false,
                    schema = @Schema(type = "boolean", nullable = true)
            )
            @RequestParam(required = false) Boolean done) {
        List<Todo> todos = this.todoService.getTodoListByMember(principal.id(), done);
        return todoMapper.toTodoMainResponseList(todos);
    }

    /**
     * 특정 Todo 조회
     *
     * <p>고유 식별자를 이용해 특정 Todo 항목을 조회합니다.
     *
     * @param id 조회할 Todo의 고유 식별자
     * @return 해당 id를 갖는 Todo
     */
    @Operation(summary = "특정 Todo 조회", description = "ID에 해당하는 Todo를 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 Todo를 조회함", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class))), @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)})
    @GetMapping("/me/todos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TodoMainResponse getTodo(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "조회할 Todo의 ID") @PathVariable Long id
    ) {
        Todo todo = todoService.getTodoByMemberIdAndTodoId(principal.id(), id);
        return todoMapper.toTodoMainResponse(todo);
    }

    /**
     * Todo 등록
     *
     * <p>새로운 Todo 항목을 등록합니다.
     *
     * @param createTodoRequest 등록할 Todo 객체
     */
    @Operation(summary = "Todo 등록", description = "새로운 Todo를 등록합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "성공적으로 Todo를 생성함", content = @Content), @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)})
    @PostMapping("/me/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody @Valid CreateTodoRequest createTodoRequest) {
        Todo todo = todoMapper.toTodo(createTodoRequest);
        this.todoService.createTodo(principal.id(), todo);
    }

    /**
     * Todo 수정
     *
     * <p>기존 Todo 항목의 내용을 수정합니다.
     *
     * @param id                수정할 Todo의 고유 식별자
     * @param updateTodoRequest 수정 내용이 담긴 Todo 객체
     */
    @Operation(summary = "Todo 수정", description = "ID에 해당하는 Todo를 수정합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "성공적으로 Todo를 수정함", content = @Content), @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content), @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)})
    @PutMapping("/me/todos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modify(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateTodoRequest updateTodoRequest) {
        Todo todo = todoMapper.toTodo(updateTodoRequest, id);
        todo.setId(id);
        this.todoService.updateTodo(principal.id(), todo);
    }

    /**
     * Todo 삭제
     *
     * <p>기존에 등록된 Todo 항목을 삭제합니다.
     *
     * @param id 삭제할 Todo의 고유 식별자
     */
    @Operation(summary = "Todo 삭제", description = "ID에 해당하는 Todo를 삭제합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "성공적으로 Todo를 삭제함", content = @Content), @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)})
    @DeleteMapping("/me/todos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id) {
        this.todoService.deleteTodo(principal.id(), id);
    }
}
