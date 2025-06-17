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
    if (todoRepository.count() > 0) {
      log.debug("Member {} already has todos, skipping initialization", member.getNickname());
      return;
    }

    Category guideCategory = categoryRepository.findByNameAndMemberId("가이드", member.getId())
        .orElseGet(() -> {
          Category category = Category.builder()
              .name("가이드")
              .member(member)
              .build();
          return categoryRepository.save(category);
        });

    var defaultTodos = List.of(
        Todo.builder()
            .title("ZZIC에 오신 것을 환영합니다!")
            .description("투두 리스트를 활용해 효율적으로 일정을 관리해보세요.")
            .priority(2)
            .category(guideCategory)
            .member(member)
            .build(),
        Todo.builder()
            .title("첫 번째 할일 완료하기")
            .description("이 할일을 완료 상태로 변경해보세요.")
            .priority(1)
            .category(guideCategory)
            .dueDate(LocalDate.now().plusDays(2))
            .member(member)
            .build(),
        Todo.builder()
            .title("카테고리 만들기")
            .description("업무, 개인, 공부 등 카테고리를 만들어 할일을 분류해보세요.")
            .priority(1)
            .member(member)
            .build(),
        Todo.builder()
            .title("마감일 설정하기")
            .description("중요한 할일에는 마감일을 설정해보세요.")
            .priority(0)
            .dueDate(LocalDate.now().plusDays(7))
            .member(member)
            .build(),
        Todo.builder()
            .title("태그 활용하기")
            .description("태그를 활용해 할일을 더 세밀하게 분류해보세요.")
            .priority(0)
            .tags(Set.of("가이드", "시작"))
            .member(member)
            .build()
    );

    todoRepository.saveAll(defaultTodos);
    log.info("Created {} default todos for member {}", defaultTodos.size(), member.getNickname());
  }
}
