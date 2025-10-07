package point.ttodoApi.todo.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.shared.exception.ResourceNotFoundException;
import point.ttodoApi.todo.application.command.CreateTodoDefinitionCommand;
import point.ttodoApi.todo.application.command.UpdateTodoDefinitionCommand;
import point.ttodoApi.todo.domain.TodoDefinition;
import point.ttodoApi.todo.domain.TodoInstance;
import point.ttodoApi.todo.infrastructure.persistence.TodoDefinitionRepository;
import point.ttodoApi.todo.infrastructure.persistence.TodoInstanceRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoDefinitionService {

  private final TodoDefinitionRepository definitionRepository;
  private final TodoInstanceRepository instanceRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  /**
   * 투두 정의 생성
   */
  @Transactional
  public TodoDefinition createDefinition(UUID userId, CreateTodoDefinitionCommand command) {
    log.debug("Creating todo definition for user: {} with command: {}", userId, command);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    Category category = null;
    if (command.getCategoryId() != null) {
      category = categoryRepository.findById(command.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + command.getCategoryId()));
    }

    // 정의 생성
    TodoDefinition definition = TodoDefinition.builder()
        .title(command.getTitle())
        .description(command.getDescription())
        .priorityId(command.getPriorityId())
        .category(category)
        .tags(command.getTags() != null ? new HashSet<>(command.getTags()) : new HashSet<>())
        .recurrenceRule(command.getRecurrenceRule())
        .baseDate(command.getBaseDate())
        .baseTime(command.getBaseTime())
        .isCollaborative(command.getIsCollaborative() != null ? command.getIsCollaborative() : false)
        .owner(owner)
        .build();

    definition = definitionRepository.save(definition);
    log.debug("Todo definition created with id: {}", definition.getId());

    // 인스턴스 생성
    createInstances(definition, command);

    return definition;
  }

  /**
   * 투두 정의 수정
   */
  @Transactional
  public TodoDefinition updateDefinition(UUID userId, UUID definitionId, UpdateTodoDefinitionCommand command) {
    log.debug("Updating todo definition: {} for user: {}", definitionId, userId);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoDefinition definition = definitionRepository.findByIdAndOwner(definitionId, owner)
        .orElseThrow(() -> new ResourceNotFoundException("Todo definition not found or access denied"));

    if (!definition.isEditableBy(owner)) {
      throw new IllegalStateException("User cannot edit this definition");
    }

    // 카테고리 업데이트
    if (command.getCategoryId() != null) {
      Category category = categoryRepository.findById(command.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + command.getCategoryId()));
      definition.setCategory(category);
    }

    // 필드 업데이트
    if (command.getTitle() != null) {
      definition.setTitle(command.getTitle());
    }
    if (command.getDescription() != null) {
      definition.setDescription(command.getDescription());
    }
    if (command.getPriorityId() != null) {
      definition.setPriorityId(command.getPriorityId());
    }
    if (command.getTags() != null) {
      definition.setTags(new HashSet<>(command.getTags()));
    }
    if (command.getRecurrenceRule() != null) {
      definition.setRecurrenceRule(command.getRecurrenceRule());
    }
    if (command.getBaseDate() != null) {
      definition.setBaseDate(command.getBaseDate());
    }
    if (command.getBaseTime() != null) {
      definition.setBaseTime(command.getBaseTime());
    }
    if (command.getIsCollaborative() != null) {
      definition.setIsCollaborative(command.getIsCollaborative());
    }

    definition = definitionRepository.save(definition);
    log.debug("Todo definition updated: {}", definitionId);

    // 미래 인스턴스 업데이트 필요 시 처리
    if (command.getUpdateFutureInstances() != null && command.getUpdateFutureInstances()) {
      updateFutureInstances(definition);
    }

    return definition;
  }

  /**
   * 투두 정의 소프트 삭제
   */
  @Transactional
  public void deleteDefinition(UUID userId, UUID definitionId) {
    log.debug("Deleting todo definition: {} for user: {}", definitionId, userId);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoDefinition definition = definitionRepository.findByIdAndOwner(definitionId, owner)
        .orElseThrow(() -> new ResourceNotFoundException("Todo definition not found or access denied"));

    if (!definition.isEditableBy(owner)) {
      throw new IllegalStateException("User cannot delete this definition");
    }

    // 소프트 삭제
    definition.softDelete();
    definitionRepository.save(definition);

    // 관련 인스턴스들도 소프트 삭제
    instanceRepository.softDeleteByDefinition(definition, LocalDateTime.now());

    log.info("Todo definition soft deleted: {}", definitionId);
  }

  /**
   * 투두 정의 복구
   */
  @Transactional
  public TodoDefinition restoreDefinition(UUID userId, UUID definitionId) {
    log.debug("Restoring todo definition: {} for user: {}", definitionId, userId);

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoDefinition definition = definitionRepository.findById(definitionId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo definition not found"));

    if (!definition.getOwner().equals(owner)) {
      throw new IllegalStateException("User cannot restore this definition");
    }

    // 복구
    definition.restore();
    definitionRepository.save(definition);

    log.info("Todo definition restored: {}", definitionId);
    return definition;
  }

  /**
   * 정의별 조회
   */
  @Transactional(readOnly = true)
  public TodoDefinition getDefinition(UUID userId, UUID definitionId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    TodoDefinition definition = definitionRepository.findById(definitionId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo definition not found: " + definitionId));

    if (!definition.isAccessibleBy(user)) {
      throw new IllegalStateException("User cannot access this definition");
    }

    return definition;
  }

  /**
   * 사용자의 모든 정의 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoDefinition> getUserDefinitions(UUID userId, Pageable pageable) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return definitionRepository.findActiveByOwnerPageable(owner, pageable);
  }

  /**
   * 반복 정의만 조회
   */
  @Transactional(readOnly = true)
  public List<TodoDefinition> getRecurringDefinitions(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return definitionRepository.findRecurringByOwner(owner);
  }

  /**
   * 1회용 정의만 조회
   */
  @Transactional(readOnly = true)
  public List<TodoDefinition> getSingleDefinitions(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return definitionRepository.findSingleByOwner(owner);
  }

  /**
   * 인스턴스 생성 헬퍼
   */
  private void createInstances(TodoDefinition definition, CreateTodoDefinitionCommand command) {
    if (definition.getRecurrenceRule() == null) {
      // 1회용 투두 - 단일 인스턴스 생성
      createSingleInstance(definition, command.getBaseDate(), command.getBaseTime());
    } else {
      // 반복 투두 - 여러 인스턴스 생성
      createRecurringInstances(definition, command);
    }
  }

  /**
   * 단일 인스턴스 생성
   */
  private void createSingleInstance(TodoDefinition definition, LocalDate dueDate, LocalTime dueTime) {
    TodoInstance instance = TodoInstance.builder()
        .definition(definition)
        .sequenceNumber(1)
        .owner(definition.getOwner())
        .dueDate(dueDate != null ? dueDate : LocalDate.now())
        .dueTime(dueTime)
        .build();

    instanceRepository.save(instance);
    log.debug("Single instance created for definition: {}", definition.getId());
  }

  /**
   * 반복 인스턴스 생성
   */
  private void createRecurringInstances(TodoDefinition definition, CreateTodoDefinitionCommand command) {
    // 반복 규칙 파싱 및 인스턴스 생성 로직
    // RFC 5545 기반 구현 필요
    try {
      Map<String, Object> rule = objectMapper.readValue(definition.getRecurrenceRule(), Map.class);
      String frequency = (String) rule.get("frequency");
      Integer interval = (Integer) rule.getOrDefault("interval", 1);
      Integer count = (Integer) rule.getOrDefault("count", 10); // 기본 10회

      LocalDate startDate = command.getBaseDate() != null ? command.getBaseDate() : LocalDate.now();
      LocalTime startTime = command.getBaseTime();

      for (int i = 0; i < count; i++) {
        LocalDate instanceDate = calculateNextDate(startDate, frequency, interval * i);

        TodoInstance instance = TodoInstance.builder()
            .definition(definition)
            .sequenceNumber(i + 1)
            .owner(definition.getOwner())
            .dueDate(instanceDate)
            .dueTime(startTime)
            .build();

        instanceRepository.save(instance);
      }

      log.debug("Created {} recurring instances for definition: {}", count, definition.getId());
    } catch (Exception e) {
      log.error("Failed to parse recurrence rule: {}", definition.getRecurrenceRule(), e);
      // 실패 시 단일 인스턴스라도 생성
      createSingleInstance(definition, command.getBaseDate(), command.getBaseTime());
    }
  }

  /**
   * 다음 날짜 계산
   */
  private LocalDate calculateNextDate(LocalDate baseDate, String frequency, int offset) {
    return switch (frequency.toUpperCase()) {
      case "DAILY" -> baseDate.plusDays(offset);
      case "WEEKLY" -> baseDate.plusWeeks(offset);
      case "MONTHLY" -> baseDate.plusMonths(offset);
      case "YEARLY" -> baseDate.plusYears(offset);
      default -> baseDate.plusDays(offset);
    };
  }

  /**
   * 미래 인스턴스 업데이트
   */
  private void updateFutureInstances(TodoDefinition definition) {
    LocalDate today = LocalDate.now();
    List<TodoInstance> futureInstances = instanceRepository.findByDefinition(definition)
        .stream()
        .filter(i -> i.getDueDate().isAfter(today) && !i.isDeleted())
        .toList();

    for (TodoInstance instance : futureInstances) {
      // definition의 값으로 인스턴스 업데이트 (오버라이드 값이 없는 경우)
      if (instance.getTitle() == null) {
        instance.setTitle(null); // effective value will use definition's
      }
      if (instance.getDescription() == null) {
        instance.setDescription(null);
      }
      if (instance.getPriorityId() == null) {
        instance.setPriorityId(null);
      }
      instanceRepository.save(instance);
    }

    log.debug("Updated {} future instances for definition: {}", futureInstances.size(), definition.getId());
  }

  /**
   * 통계 조회
   */
  @Transactional(readOnly = true)
  public Map<String, Long> getDefinitionStats(UUID userId) {
    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return definitionRepository.getStatsByOwner(owner);
  }
}