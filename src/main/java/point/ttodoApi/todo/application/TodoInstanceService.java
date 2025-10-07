package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.experience.application.event.TodoCompletedEvent;
import point.ttodoApi.experience.application.event.TodoUncompletedEvent;
import point.ttodoApi.shared.exception.ResourceNotFoundException;
import point.ttodoApi.todo.application.command.UpdateTodoInstanceCommand;
import point.ttodoApi.todo.domain.TodoInstance;
import point.ttodoApi.todo.infrastructure.persistence.TodoInstanceRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoInstanceService {

  private final TodoInstanceRepository instanceRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public TodoInstance getInstance(UUID userId, UUID instanceId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findById(instanceId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found: " + instanceId));

    if (!instance.isAccessibleBy(user)) {
      throw new IllegalStateException("User cannot access this instance");
    }

    return instance;
  }

  /**
   * 오늘의 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getTodayInstances(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findTodayInstances(owner, LocalDate.now());
  }

  /**
   * 날짜 범위로 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getInstancesByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findByDateRange(owner, startDate, endDate);
  }

  /**
   * 지난 미완료 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getOverdueInstances(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findOverdueInstances(owner, LocalDate.now());
  }

  /**
   * 예정된 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoInstance> getUpcomingInstances(UUID userId, Pageable pageable) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findUpcomingInstances(owner, LocalDate.now(), pageable);
  }

  /**
   * 완료된 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoInstance> getCompletedInstances(UUID userId, Pageable pageable) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findCompletedInstances(owner, pageable);
  }

  /**
   * 고정된 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getPinnedInstances(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findPinnedInstances(owner);
  }

  /**
   * 인스턴스 수정 (오버라이드)
   */
  @Transactional
  public TodoInstance updateInstance(UUID userId, UUID instanceId, UpdateTodoInstanceCommand command) {
    log.debug("Updating todo instance: {} for user: {}", instanceId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findById(instanceId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found: " + instanceId));

    if (!instance.isEditableBy(user)) {
      throw new IllegalStateException("User cannot edit this instance");
    }

    // 오버라이드 필드 업데이트
    if (command.getTitle() != null) {
      instance.setTitle(command.getTitle());
    }
    if (command.getDescription() != null) {
      instance.setDescription(command.getDescription());
    }
    if (command.getPriorityId() != null) {
      instance.setPriorityId(command.getPriorityId());
    }
    if (command.getCategoryId() != null) {
      Category category = categoryRepository.findById(command.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + command.getCategoryId()));
      instance.setCategory(category);
    }
    if (command.getTags() != null) {
      instance.setTags(new HashSet<>(command.getTags()));
    }

    // 인스턴스 고유 필드 업데이트
    if (command.getDueDate() != null) {
      instance.setDueDate(command.getDueDate());
    }
    if (command.getDueTime() != null) {
      instance.setDueTime(command.getDueTime());
    }
    if (command.getIsPinned() != null) {
      instance.setIsPinned(command.getIsPinned());
    }
    if (command.getDisplayOrder() != null) {
      instance.setDisplayOrder(command.getDisplayOrder());
    }

    instance = instanceRepository.save(instance);
    log.debug("Todo instance updated: {}", instanceId);

    return instance;
  }

  /**
   * 인스턴스 완료 처리
   */
  @Transactional
  public TodoInstance completeInstance(UUID userId, UUID instanceId) {
    log.debug("Completing todo instance: {} for user: {}", instanceId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    if (instance.isCompleted()) {
      throw new IllegalStateException("Instance already completed");
    }

    instance.markComplete();
    instance = instanceRepository.save(instance);

    // 경험치 이벤트 발행
    eventPublisher.publishEvent(new TodoCompletedEvent(
        user.getId(),
        instance.getId(),
        instance.getEffectivePriorityId()
    ));

    log.info("Todo instance completed: {}", instanceId);
    return instance;
  }

  /**
   * 인스턴스 완료 취소
   */
  @Transactional
  public TodoInstance uncompleteInstance(UUID userId, UUID instanceId) {
    log.debug("Uncompleting todo instance: {} for user: {}", instanceId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    if (!instance.isCompleted()) {
      throw new IllegalStateException("Instance is not completed");
    }

    instance.markIncomplete();
    instance = instanceRepository.save(instance);

    // 경험치 차감 이벤트 발행
    eventPublisher.publishEvent(new TodoUncompletedEvent(
        user.getId(),
        instance.getId(),
        instance.getEffectivePriorityId()
    ));

    log.info("Todo instance uncompleted: {}", instanceId);
    return instance;
  }

  /**
   * 인스턴스 소프트 삭제
   */
  @Transactional
  public void deleteInstance(UUID userId, UUID instanceId) {
    log.debug("Deleting todo instance: {} for user: {}", instanceId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    if (!instance.isEditableBy(user)) {
      throw new IllegalStateException("User cannot delete this instance");
    }

    instance.softDelete();
    instanceRepository.save(instance);

    log.info("Todo instance soft deleted: {}", instanceId);
  }

  /**
   * 인스턴스 복구
   */
  @Transactional
  public TodoInstance restoreInstance(UUID userId, UUID instanceId) {
    log.debug("Restoring todo instance: {} for user: {}", instanceId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findById(instanceId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found"));

    if (!instance.getOwner().equals(user)) {
      throw new IllegalStateException("User cannot restore this instance");
    }

    instance.restore();
    instance = instanceRepository.save(instance);

    log.info("Todo instance restored: {}", instanceId);
    return instance;
  }

  /**
   * 인스턴스 토글 (완료/미완료)
   */
  @Transactional
  public TodoInstance toggleInstance(UUID userId, UUID instanceId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    if (instance.isCompleted()) {
      return uncompleteInstance(userId, instanceId);
    } else {
      return completeInstance(userId, instanceId);
    }
  }

  /**
   * 통계 조회
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getCompletionStats(UUID userId, LocalDate startDate, LocalDate endDate) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.getCompletionStatsByDateRange(owner, startDate, endDate);
  }

  /**
   * 카테고리별 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getInstancesByCategory(UUID userId, UUID categoryId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findByCategoryId(owner, categoryId);
  }

  /**
   * 인스턴스 생성 (수동)
   */
  @Transactional
  public TodoInstance createInstance(UUID userId, point.ttodoApi.todo.application.command.CreateTodoInstanceCommand command) {
    log.debug("Creating todo instance for user: {}", userId);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // TODO: Implement instance creation logic
    throw new UnsupportedOperationException("createInstance not yet implemented");
  }

  /**
   * 상태 업데이트 (완료/미완료 토글)
   */
  @Transactional
  public TodoInstance updateStatus(UUID userId, UUID instanceId, point.ttodoApi.todo.application.command.UpdateTodoStatusCommand command) {
    log.debug("Updating status for instance: {}", instanceId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    // UpdateTodoStatusCommand는 사실상 불필요하지만 호환성을 위해 유지
    // 실제로는 completeInstance/uncompleteInstance 사용 권장

    return instanceRepository.save(instance);
  }

  /**
   * 고정 토글
   */
  @Transactional
  public TodoInstance togglePin(UUID userId, UUID instanceId, boolean pin) {
    log.debug("Toggling pin for instance: {} to {}", instanceId, pin);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoInstance instance = instanceRepository.findByIdAndOwner(instanceId, user)
        .orElseThrow(() -> new ResourceNotFoundException("Todo instance not found or access denied"));

    instance.setIsPinned(pin);
    return instanceRepository.save(instance);
  }

  /**
   * 정의의 모든 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoInstance> getDefinitionInstances(UUID userId, UUID definitionId, Pageable pageable) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // TODO: Implement repository method
    throw new UnsupportedOperationException("getDefinitionInstances not yet implemented");
  }

  /**
   * 특정 날짜의 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public List<TodoInstance> getInstancesByDate(UUID userId, LocalDate date) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return instanceRepository.findByDateRange(owner, date, date);
  }

  /**
   * 상태별 인스턴스 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoInstance> getInstancesByStatus(UUID userId, Integer statusId, Pageable pageable) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // TODO: Implement repository method
    throw new UnsupportedOperationException("getInstancesByStatus not yet implemented");
  }

  /**
   * 반복 정의의 인스턴스 대량 생성
   */
  @Transactional
  public List<TodoInstance> generateInstances(UUID userId, UUID definitionId, LocalDate startDate, LocalDate endDate) {
    log.debug("Generating instances for definition: {} from {} to {}", definitionId, startDate, endDate);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // TODO: Implement instance generation logic
    throw new UnsupportedOperationException("generateInstances not yet implemented");
  }
}