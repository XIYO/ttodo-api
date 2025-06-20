package point.zzicback.todo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodoInitializer implements ApplicationRunner {
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public void run(ApplicationArguments args) {
    log.info("Todo initialization ready!");
  }

  public void createDefaultTodosForMember(Member member) {
    if (!"anon@zzic.com".equals(member.getEmail())) {
      log.debug("Member {} is not the target anon user, skipping initialization", member.getNickname());
      return;
    }
    
    if (todoRepository.countByMemberId(member.getId()) > 0) {
      log.debug("Member {} already has todos, skipping initialization", member.getNickname());
      return;
    }

    createDummyCategoriesForMember(member);
    
    List<Category> categories = categoryRepository.findByMemberIdOrderByNameAsc(member.getId());
    Map<String, Category> categoryMap = new HashMap<>();
    for (Category c : categories) categoryMap.put(c.getName(), c);

    List<Todo> defaultTodos = List.of(
        Todo.builder()
            .title("첫 번째 할일 완료하기")
            .description("이 할일을 완료 상태로 변경해보세요. 예: 오늘 할일을 체크해보세요.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("시작", "튜토리얼"))
            .member(member)
            .build(),
        Todo.builder()
            .title("업무 회의 준비")
            .description("업무 카테고리에 회의 준비 자료를 정리해보세요. 예: 회의 안건 정리, 자료 출력 등")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("회의", "업무", "중요"))
            .member(member)
            .build(),
        Todo.builder()
            .title("영어 단어 암기")
            .description("하루에 영어 단어 10개 외우기")
            .priorityId(0)
            .dueDate(LocalDate.now().plusDays(7))
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .tags(Set.of("영어", "학습", "반복"))
            .member(member)
            .build(),
        Todo.builder()
            .title("가족 일정 추가하기")
            .description("저녁 6시에 가족들과 식사.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .tags(Set.of("가족", "식사", "저녁"))
            .member(member)
            .build(),
        Todo.builder()
            .title("약속 관리하기")
            .description("친구들과 점심 약속.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .tags(Set.of("친구", "점심", "사교"))
            .member(member)
            .build(),
        Todo.builder()
            .title("운동 계획 세우기")
            .description("복근 운동 3세트, 유산소 20분")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .tags(Set.of("운동", "건강", "복근"))
            .member(member)
            .build()
    );

    todoRepository.saveAll(defaultTodos);
    log.info("Created {} default todos for member {}", defaultTodos.size(), member.getNickname());
  }

  public void createDummyCategoriesForMember(Member member) {
    var dummyCategories = List.of(
        Category.builder().name("약속").member(member).build(),
        Category.builder().name("가족").member(member).build(),
        Category.builder().name("공부").member(member).build(),
        Category.builder().name("운동").member(member).build()
    );
    categoryRepository.saveAll(dummyCategories);
  }
}
