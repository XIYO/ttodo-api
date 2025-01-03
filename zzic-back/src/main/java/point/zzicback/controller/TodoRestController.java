package point.zzicback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import point.zzicback.dto.request.CreateTodoRequest;
import point.zzicback.dto.request.UpdateTodoRequest;
import point.zzicback.dto.response.TodoMainResponse;
import point.zzicback.model.Todo;
import point.zzicback.service.TodoService;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Todo API", description = "To-Do 목록을 조회, 등록, 수정, 삭제하는 API")
@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoRestController {

    private final TodoService todoService;

    /**
     * Todo 목록 조회
     */
    @Operation(summary = "Todo 목록 조회", description = "모든 Todo 항목의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 Todo 목록을 조회함",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TodoMainResponse.class))))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TodoMainResponse> getAll() {
        List<Todo> todos = this.todoService.getAll();
        return todos.stream()
                .map(TodoMainResponse::fromEntity)
                .toList();
    }

    /**
     * 특정 Todo 조회
     */
    @Operation(summary = "특정 Todo 조회", description = "ID에 해당하는 Todo를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 Todo를 조회함", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoMainResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Todo getById(@Parameter(description = "조회할 Todo의 ID") @PathVariable Long id) {
        Todo todo = this.todoService.getById(id);
        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID " + id + " not found");
        }
        return todo;
    }

    /**
     * Todo 등록
     */
    @Operation(summary = "Todo 등록", description = "새로운 Todo를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 Todo를 생성함", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Parameter(description = "등록할 Todo 정보") @Valid @RequestBody CreateTodoRequest createTodoRequest) {
        Todo todo = createTodoRequest.toEntity();
        this.todoService.add(todo);
    }

    /**
     * Todo 수정
     */
    @Operation(summary = "Todo 수정", description = "ID에 해당하는 Todo를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 Todo를 수정함", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modify(@Parameter(description = "수정할 Todo의 ID") @PathVariable Long id,
                       @Parameter(description = "수정할 Todo 정보") @Valid @RequestBody UpdateTodoRequest updateTodoRequest) {
        int result = this.todoService.modify(updateTodoRequest.toEntity(id));
        if (result == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID " + id + " not found");
        }
    }

    /**
     * Todo 삭제
     */
    @Operation(summary = "Todo 삭제", description = "ID에 해당하는 Todo를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 Todo를 삭제함", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 Todo를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@Parameter(description = "삭제할 Todo의 ID") @PathVariable Long id) {
        Todo todo = this.todoService.getById(id);
        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID " + id + " not found");
        }
        this.todoService.remove(id);
    }
}
