package point.ttodoApi.shared.bootstrap.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.todo.domain.TodoTemplate;
import point.ttodoApi.todo.domain.recurrence.Frequency;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.domain.recurrence.WeekDay;
import point.ttodoApi.todo.infrastructure.persistence.TodoTemplateRepository;

import java.time.LocalDate;
import java.util.*;

import static point.ttodoApi.shared.constants.SystemConstants.SystemUsers.ANON_USER_ID;

/**
 * 할일 템플릿 초기 데이터 생성
 * 익명 사용자를 위한 샘플 할일과 카테고리 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TodoDataBootstrap {

    private final TodoTemplateRepository todoTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository UserRepository;

    public void initialize() {
        var anonUser = UserRepository.findById(ANON_USER_ID).orElse(null);
        if (anonUser == null) {
            log.debug("Anonymous user not found, skipping todo initialization");
            return;
        }

        if (!"anon@ttodo.dev".equals(anonUser.getEmail())) {
            log.debug("User {} is not the target anon user, skipping initialization", anonUser.getEmail());
            return;
        }

        if (!todoTemplateRepository.findByOwnerId(anonUser.getId()).isEmpty()) {
            log.info("User {} already has todos, skipping initialization", anonUser.getEmail());
            return;
        }

        // 기본 카테고리 생성
        createDefaultCategories(anonUser);
        
        // 카테고리 맵 생성
        Map<String, Category> categoryMap = getCategoryMap(anonUser.getId());
        
        // 샘플 할일 생성
        List<TodoTemplate> sampleTodos = createSampleTodos(anonUser, categoryMap);
        todoTemplateRepository.saveAll(sampleTodos);

        log.info("Created {} sample todos for user {}", sampleTodos.size(), anonUser.getEmail());
    }

    /**
     * 기본 카테고리 생성
     */
    private void createDefaultCategories(User owner) {
        List<Category> categories = List.of(
                Category.builder()
                        .name("개인")
                        .color(null)
                        .description("개인적인 할일")
                        .owner(owner)
                        .build(),
                Category.builder()
                        .name("업무")
                        .color(null)
                        .description("업무 관련 할일")
                        .owner(owner)
                        .build(),
                Category.builder()
                        .name("공부")
                        .color(null)
                        .description("학습 관련 할일")
                        .owner(owner)
                        .build(),
                Category.builder()
                        .name("가족")
                        .color(null)
                        .description("가족 관련 할일")
                        .owner(owner)
                        .build(),
                Category.builder()
                        .name("약속")
                        .color(null)
                        .description("약속 및 만남")
                        .owner(owner)
                        .build(),
                Category.builder()
                        .name("운동")
                        .color(null)
                        .description("운동 및 건강")
                        .owner(owner)
                        .build()
        );
        categoryRepository.saveAll(categories);
    }

    /**
     * 카테고리 이름 → 카테고리 매핑 생성
     */
    private Map<String, Category> getCategoryMap(UUID ownerId) {
        List<Category> categories = categoryRepository.findByOwnerIdOrderByNameAsc(ownerId);
        Map<String, Category> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getName(), c);
        }
        return categoryMap;
    }

    /**
     * 샘플 할일 템플릿 생성 (기존 970줄 → 간소화)
     */
    private List<TodoTemplate> createSampleTodos(User owner, Map<String, Category> categoryMap) {
        List<TodoTemplate> todos = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // 튜토리얼 할일
        todos.add(TodoTemplate.builder()
                .title("첫 번째 할일 완료하기")
                .description("이 할일을 완료 상태로 변경해보세요. 예: 오늘 할일을 체크해보세요.")
                .priorityId(1)
                .category(categoryMap.get("개인"))
                .date(now.plusDays(2))
                .tags(Set.of("시작", "튜토리얼"))
                .owner(owner)
                .build());

        // 개인 카테고리 할일들
        todos.addAll(createPersonalTodos(owner, categoryMap.get("개인"), now));
        
        // 업무 카테고리 할일들
        todos.addAll(createWorkTodos(owner, categoryMap.get("업무"), now));
        
        // 공부 카테고리 할일들
        todos.addAll(createStudyTodos(owner, categoryMap.get("공부"), now));

        // 기타 카테고리별 할일들 (간소화)
        todos.addAll(createOtherTodos(owner, categoryMap, now));

        return todos;
    }

    /**
     * 개인 카테고리 할일 생성
     */
    private List<TodoTemplate> createPersonalTodos(User owner, Category category, LocalDate now) {
        List<TodoTemplate> todos = new ArrayList<>();

        // 반복 할일 - 매일 명상
        TodoTemplate meditation = TodoTemplate.builder()
                .title("아침 명상하기")
                .description("하루를 시작하기 전 15분 명상으로 마음 정리하기")
                .priorityId(2)
                .category(category)
                .date(now)
                .tags(Set.of("명상", "습관", "아침"))
                .owner(owner)
                .build();
        RecurrenceRule dailyRule = new RecurrenceRule();
        dailyRule.setFrequency(Frequency.DAILY);
        dailyRule.setInterval(1);
        dailyRule.setAnchorDate(now);
        meditation.setRecurrenceRule(dailyRule);
        meditation.setAnchorDate(dailyRule.getAnchorDate());
        todos.add(meditation);

        // 반복 할일 - 주간 목표 설정
        TodoTemplate weeklyGoal = TodoTemplate.builder()
                .title("주간 목표 설정하기")
                .description("이번 주 성취할 3가지 목표 구체적으로 작성하기")
                .priorityId(2)
                .category(category)
                .date(now.minusDays(3))
                .tags(Set.of("목표", "계획", "성장"))
                .owner(owner)
                .build();
        RecurrenceRule weeklyRule = new RecurrenceRule();
        weeklyRule.setFrequency(Frequency.WEEKLY);
        weeklyRule.setInterval(1);
        weeklyRule.setByWeekDays(EnumSet.of(WeekDay.MO));
        weeklyRule.setAnchorDate(now);
        weeklyGoal.setRecurrenceRule(weeklyRule);
        weeklyGoal.setAnchorDate(weeklyRule.getAnchorDate());
        todos.add(weeklyGoal);

        // 일반 할일들
        todos.add(TodoTemplate.builder()
                .title("일기 작성하기")
                .description("오늘 하루 있었던 일과 감정 정리하기")
                .priorityId(1)
                .category(category)
                .date(now)
                .tags(Set.of("일기", "성찰", "습관"))
                .owner(owner)
                .build());

        todos.add(TodoTemplate.builder()
                .title("책 읽기")
                .description("오늘 30페이지 읽고 중요 내용 메모하기")
                .priorityId(1)
                .category(category)
                .date(now.plusDays(1))
                .tags(Set.of("독서", "성장", "지식"))
                .owner(owner)
                .build());

        todos.add(TodoTemplate.builder()
                .title("방 청소하기")
                .description("옷장 정리 및 책상 먼지 제거하기")
                .priorityId(1)
                .category(category)
                .date(now.minusDays(1))
                .tags(Set.of("청소", "정리", "집"))
                .owner(owner)
                .build());

        return todos;
    }

    /**
     * 업무 카테고리 할일 생성
     */
    private List<TodoTemplate> createWorkTodos(User owner, Category category, LocalDate now) {
        return List.of(
                TodoTemplate.builder()
                        .title("업무 회의 준비")
                        .description("회의 안건 정리 및 자료 출력")
                        .priorityId(2)
                        .category(category)
                        .date(now.plusDays(1))
                        .tags(Set.of("회의", "업무", "중요"))
                        .owner(owner)
                        .build(),
                TodoTemplate.builder()
                        .title("주간 업무 보고서 작성")
                        .description("지난 주 성과와 다음 주 계획 포함")
                        .priorityId(2)
                        .category(category)
                        .date(now.minusDays(3))
                        .tags(Set.of("보고서", "업무", "마감"))
                        .owner(owner)
                        .build(),
                TodoTemplate.builder()
                        .title("이메일 정리하기")
                        .description("미처리 이메일 답장 및 중요 메일 폴더 정리")
                        .priorityId(1)
                        .category(category)
                        .date(now.minusDays(1))
                        .tags(Set.of("이메일", "정리", "커뮤니케이션"))
                        .owner(owner)
                        .build()
        );
    }

    /**
     * 공부 카테고리 할일 생성
     */
    private List<TodoTemplate> createStudyTodos(User owner, Category category, LocalDate now) {
        return List.of(
                TodoTemplate.builder()
                        .title("영어 단어 암기")
                        .description("하루에 영어 단어 10개 외우기")
                        .priorityId(0)
                        .category(category)
                        .date(now.plusDays(7))
                        .tags(Set.of("영어", "학습", "반복"))
                        .owner(owner)
                        .build(),
                TodoTemplate.builder()
                        .title("알고리즘 문제 풀기")
                        .description("프로그래머스 Level 2 문제 3개 풀기")
                        .priorityId(1)
                        .category(category)
                        .date(now)
                        .tags(Set.of("알고리즘", "코딩", "문제해결"))
                        .owner(owner)
                        .build(),
                TodoTemplate.builder()
                        .title("스프링 부트 강의 듣기")
                        .description("인프런 강의 Section 4 완료하기")
                        .priorityId(1)
                        .category(category)
                        .date(now.plusDays(1))
                        .tags(Set.of("개발", "스프링", "강의"))
                        .owner(owner)
                        .build()
        );
    }

    /**
     * 기타 카테고리 할일들 생성 (간소화)
     */
    private List<TodoTemplate> createOtherTodos(User owner, Map<String, Category> categoryMap, LocalDate now) {
        List<TodoTemplate> todos = new ArrayList<>();

        // 가족 카테고리
        todos.add(TodoTemplate.builder()
                .title("가족 저녁 식사")
                .description("저녁 6시에 가족들과 식사")
                .priorityId(1)
                .category(categoryMap.get("가족"))
                .date(now)
                .tags(Set.of("가족", "식사", "저녁"))
                .owner(owner)
                .build());

        // 약속 카테고리
        todos.add(TodoTemplate.builder()
                .title("친구들과 점심 약속")
                .description("친구들과 점심 약속")
                .priorityId(1)
                .category(categoryMap.get("약속"))
                .date(now.plusDays(1))
                .tags(Set.of("친구", "점심", "사교"))
                .owner(owner)
                .build());

        // 운동 카테고리
        todos.add(TodoTemplate.builder()
                .title("헬스장 가기")
                .description("복근 운동 3세트, 유산소 20분")
                .priorityId(0)
                .category(categoryMap.get("운동"))
                .date(now.plusDays(1))
                .tags(Set.of("운동", "건강", "복근"))
                .owner(owner)
                .build());

        return todos;
    }
}