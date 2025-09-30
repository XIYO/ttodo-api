package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.todo.application.result.CollaborativeTodoResult;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 협업 투두 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CollaborativeTodoService {

  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository UserRepository;
  private final ProfileService profileService;

  /**
   * 멤버가 접근 가능한 모든 투두 조회 (본인 투두 + 협업 투두)
   */
  @Transactional(readOnly = true)
  public List<CollaborativeTodoResult> getAccessibleTodos(UUID userId) {
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

  List<Todo> todos = todoRepository.findAccessibleTodosByUserId(userId);

    return todos.stream()
            .map(todo -> {
              boolean canEdit = todo.isEditableBy(user);
              Profile ownerProfile = profileService.getProfile(todo.getOwner().getId());
              return CollaborativeTodoResult.from(todo, ownerProfile.getNickname(), canEdit);
            })
            .collect(Collectors.toList());
  }

  /**
   * 특정 카테고리의 협업 투두 목록 조회
   */
  @Transactional(readOnly = true)
  public List<CollaborativeTodoResult> getCollaborativeTodosByCategory(UUID categoryId, UUID userId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 카테고리 접근 권한 확인
    if (!category.canManage(user)) {
      throw new IllegalArgumentException("No permission to access category");
    }

    List<Todo> todos = todoRepository.findCollaborativeTodosByCategoryId(categoryId);

    return todos.stream()
            .map(todo -> {
              boolean canEdit = todo.isEditableBy(user);
              Profile ownerProfile = profileService.getProfile(todo.getOwner().getId());
              return CollaborativeTodoResult.from(todo, ownerProfile.getNickname(), canEdit);
            })
            .collect(Collectors.toList());
  }

  /**
   * 멤버가 협업자로 참여하는 투두 목록 조회
   */
  @Transactional(readOnly = true)
  public List<CollaborativeTodoResult> getCollaborativeTodosByUser(UUID userId) {
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

  List<Todo> todos = todoRepository.findCollaborativeTodosByUserId(userId);

    return todos.stream()
            .map(todo -> {
              boolean canEdit = todo.isEditableBy(user);
              Profile ownerProfile = profileService.getProfile(todo.getOwner().getId());
              return CollaborativeTodoResult.from(todo, ownerProfile.getNickname(), canEdit);
            })
            .collect(Collectors.toList());
  }

  /**
   * 투두를 협업 투두로 전환
   */
  public void enableTodoCollaboration(TodoId todoId, UUID userId) {
    Todo todo = findAccessibleTodo(todoId, userId);
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // owner만 협업 전환 가능
    if (!todo.getOwner().equals(user)) {
      throw new IllegalArgumentException("Only todo owner can enable collaboration");
    }

    todo.enableCollaboration();
    todoRepository.save(todo);

    log.info("Todo {} enabled collaboration by user {}", todoId, userId);
  }

  /**
   * 협업 투두를 개인 투두로 전환
   */
  public void disableTodoCollaboration(TodoId todoId, UUID userId) {
    Todo todo = findAccessibleTodo(todoId, userId);
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // owner만 협업 해제 가능
    if (!todo.getOwner().equals(user)) {
      throw new IllegalArgumentException("Only todo owner can disable collaboration");
    }

    todo.setIsCollaborative(false);
    todoRepository.save(todo);

    log.info("Todo {} disabled collaboration by user {}", todoId, userId);
  }

  /**
   * 협업 투두 완료 처리
   * 협업자도 완료할 수 있지만 수정은 카테고리 관리자만 가능
   */
  public void completeCollaborativeTodo(TodoId todoId, UUID userId) {
    Todo todo = findAccessibleTodo(todoId, userId);
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 접근 권한 확인
    if (!todo.isAccessibleBy(user)) {
      throw new IllegalArgumentException("No permission to access this todo");
    }

    // 협업 투두가 아니면 일반 완료 처리 (owner만)
    if (todo.getIsCollaborative() == null || !todo.getIsCollaborative()) {
      if (!todo.getOwner().equals(user)) {
        throw new IllegalArgumentException("Only todo owner can complete non-collaborative todo");
      }
    }

    todo.setComplete(true);
    todoRepository.save(todo);

    log.info("Todo {} completed by user {}", todoId, userId);
  }

  /**
   * 협업 투두 수정
   * 카테고리 관리 권한이 있는 사용자만 가능
   */
  public void updateCollaborativeTodo(TodoId todoId, UUID userId, String title, String description) {
    Todo todo = findAccessibleTodo(todoId, userId);
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 수정 권한 확인
    if (!todo.isEditableBy(user)) {
      throw new IllegalArgumentException("No permission to edit this todo");
    }

    if (title != null && !title.trim().isEmpty()) {
      todo.setTitle(title);
    }
    if (description != null) {
      todo.setDescription(description);
    }

    todoRepository.save(todo);

    log.info("Todo {} updated by user {}", todoId, userId);
  }

  /**
   * 협업 투두 삭제 (비활성화)
   * 카테고리 관리 권한이 있는 사용자만 가능
   */
  public void deleteCollaborativeTodo(TodoId todoId, UUID userId) {
    Todo todo = findAccessibleTodo(todoId, userId);
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 삭제 권한 확인
    if (!todo.isEditableBy(user)) {
      throw new IllegalArgumentException("No permission to delete this todo");
    }

    todo.setActive(false);
    todoRepository.save(todo);

    log.info("Todo {} deleted by user {}", todoId, userId);
  }

  /**
   * 멤버가 접근 가능한 투두 조회 헬퍼 메서드
   */
  private Todo findAccessibleTodo(TodoId todoId, UUID userId) {
    return todoRepository.findAccessibleTodoByTodoIdAndUserId(todoId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Todo not found or no access permission"));
  }

  /**
   * 투두 접근 권한 확인 (Spring Security @PreAuthorize용)
   */
  @Transactional(readOnly = true)
  public boolean canAccessTodo(TodoId todoId, UUID userId) {
    try {
      findAccessibleTodo(todoId, userId);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 투두 수정 권한 확인 (Spring Security @PreAuthorize용)
   */
  @Transactional(readOnly = true)
  public boolean canEditTodo(TodoId todoId, UUID userId) {
    try {
      Todo todo = findAccessibleTodo(todoId, userId);
      User user = UserRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
      return todo.isEditableBy(user);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 협업 투두 통계 조회
   */
  @Transactional(readOnly = true)
  public long countCollaborativeTodosByUser(UUID userId) {
  return todoRepository.countCollaborativeTodosByUserId(userId);
  }

  /**
   * 카테고리별 협업 투두 수 조회
   */
  @Transactional(readOnly = true)
  public long countCollaborativeTodosByCategory(UUID categoryId) {
    return todoRepository.countCollaborativeTodosByCategoryId(categoryId);
  }
}