package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.experience.application.event.TodoCompletedEvent;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.shared.error.EntityNotFoundException;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.application.mapper.TodoApplicationMapper;
import point.ttodoApi.todo.application.result.TodoResult;
import point.ttodoApi.todo.domain.Todo;
import point.ttodoApi.todo.domain.TodoId;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import jakarta.validation.Valid;

/**
 * Todo Command Service
 * TTODO 아키텍처 패턴: Command 처리 전용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class TodoCommandService {

    private final TodoRepository todoRepository;
    private final UserRepository UserRepository;
    private final TodoApplicationMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Todo 생성
     */
    public TodoResult createTodo(@Valid CreateTodoCommand command) {
        log.debug("Creating todo with command: {}", command);
        
        // 회원 존재 확인
        User user = UserRepository.findById(command.userId())
            .orElseThrow(() -> new EntityNotFoundException("User", command.userId()));

        // Command 검증
        command.validateRule();

        // Todo 엔티티 생성
        Todo todo = mapper.toEntity(command);
        todo.setOwner(user);

        // 저장
        Todo savedTodo = todoRepository.save(todo);
        
        log.debug("Todo created with ID: {}", savedTodo.getId());
        return mapper.toResult(savedTodo);
    }

    /**
     * Todo 수정
     */
    public TodoResult updateTodo(@Valid UpdateTodoCommand command) {
        log.debug("Updating todo with command: {}", command);
        
        // Todo 존재 확인 및 권한 체크
        TodoId todoId = new TodoId(command.originalTodoId(), command.daysDifference());
        Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new EntityNotFoundException("Todo", todoId));
            
        if (!todo.getOwner().getId().equals(command.userId())) {
            throw new IllegalArgumentException("No permission to update this todo");
        }

        // Command 검증
        command.validateRule();

        // 엔티티 업데이트
        mapper.updateEntity(todo, command);

        // 완료 상태 변경시 이벤트 발행
        if (Boolean.TRUE.equals(command.complete()) && !todo.getComplete()) {
            eventPublisher.publishEvent(new TodoCompletedEvent(command.userId(), todo.getTodoId().getId(), todo.getTitle()));
        }

        Todo updatedTodo = todoRepository.save(todo);
        
        log.debug("Todo updated with ID: {}", updatedTodo.getTodoId());
        return mapper.toResult(updatedTodo);
    }

    /**
     * Todo 삭제
     */
    public void deleteTodo(@Valid DeleteTodoCommand command) {
        log.debug("Deleting todo with command: {}", command);
        
        // Todo 존재 확인 및 권한 체크
        TodoId todoId = new TodoId(command.originalTodoId(), command.daysDifference());
        Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new EntityNotFoundException("Todo", todoId));
            
        if (!todo.getOwner().getId().equals(command.userId())) {
            throw new IllegalArgumentException("No permission to delete this todo");
        }

        todoRepository.delete(todo);
        log.debug("Todo deleted with ID: {}", todoId);
    }
}