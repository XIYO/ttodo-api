package point.ttodoApi.profile.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.utility.DockerImageName;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.profile.domain.Statistics;
import point.ttodoApi.profile.infrastructure.persistence.StatisticsRepository;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StatisticsService 통계 기능 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class StatisticsServiceTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    );

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private Member testMember;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        testMember = Member.builder()
                .email("statistics@test.com")
                .nickname("통계테스트")
                .password("password123!")
                .build();
        testMember = memberRepository.save(testMember);

        // 카테고리 생성
        category1 = Category.builder()
                .name("업무")
                .color("#FF0000")
                .owner(testMember)
                .build();
        category1 = categoryRepository.save(category1);

        category2 = Category.builder()
                .name("개인")
                .color("#00FF00")
                .owner(testMember)
                .build();
        category2 = categoryRepository.save(category2);

        setupTestData();
    }

    private void setupTestData() {
        LocalDate today = LocalDate.now();

        // 완료된 할일들 (5개)
        for (int i = 1; i <= 5; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("완료된 할일 " + i)
                    .complete(true)
                    .active(true)
                    .category(category1)
                    .owner(testMember)
                    .date(today)
                    .tags(Set.of("completed"))
                    .build();
            todoRepository.save(todo);
        }

        // 미완료 할일들 (3개) - 통계에 포함되지 않음
        for (int i = 6; i <= 8; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("미완료 할일 " + i)
                    .complete(false)
                    .active(true)
                    .category(category2)
                    .owner(testMember)
                    .date(today)
                    .tags(Set.of("incomplete"))
                    .build();
            todoRepository.save(todo);
        }
    }

    @Test
    @DisplayName("사용자 통계 조회 - 새로운 Statistics 엔티티 생성")
    void testGetStatistics_CreateNew() {
        // When
        Statistics statistics = statisticsService.getStatistics(testMember.getId());

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getSucceededTodosCount()).isEqualTo(5); // 완료된 할일 5개
        assertThat(statistics.getCategoryCount()).isEqualTo(2);       // 카테고리 2개
        assertThat(statistics.getOwner().getId()).isEqualTo(testMember.getId());
        
        // DB에 저장되었는지 확인
        assertThat(statistics.getId()).isNotNull();
        assertThat(statistics.getCreatedAt()).isNotNull();
        assertThat(statistics.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 통계 조회 - 기존 Statistics 엔티티 업데이트")
    void testGetStatistics_UpdateExisting() {
        // Given - 기존 Statistics 생성
        Statistics existingStats = Statistics.builder()
                .owner(testMember)
                .succeededTodosCount(3)
                .categoryCount(1)
                .build();
        statisticsRepository.save(existingStats);

        // When
        Statistics updatedStats = statisticsService.getStatistics(testMember.getId());

        // Then
        assertThat(updatedStats.getId()).isEqualTo(existingStats.getId()); // 같은 엔티티
        assertThat(updatedStats.getSucceededTodosCount()).isEqualTo(5);     // 업데이트된 값
        assertThat(updatedStats.getCategoryCount()).isEqualTo(2);          // 업데이트된 값
        assertThat(updatedStats.getUpdatedAt()).isAfter(updatedStats.getCreatedAt());
    }

    @Test
    @DisplayName("할일이 없는 사용자의 통계 조회")
    void testGetStatistics_NoData() {
        // Given - 새로운 사용자 생성 (할일과 카테고리 없음)
        Member newMember = Member.builder()
                .email("newuser@test.com")
                .nickname("신규사용자")
                .password("password123!")
                .build();
        newMember = memberRepository.save(newMember);

        // When
        Statistics statistics = statisticsService.getStatistics(newMember.getId());

        // Then
        assertThat(statistics.getSucceededTodosCount()).isEqualTo(0);
        assertThat(statistics.getCategoryCount()).isEqualTo(0);
        assertThat(statistics.getOwner().getId()).isEqualTo(newMember.getId());
    }

    @Test
    @DisplayName("카테고리 추가 후 통계 업데이트")
    void testGetStatistics_AfterAddingCategory() {
        // Given - 초기 통계 조회
        Statistics initialStats = statisticsService.getStatistics(testMember.getId());
        assertThat(initialStats.getCategoryCount()).isEqualTo(2);

        // When - 새 카테고리 추가
        Category newCategory = Category.builder()
                .name("운동")
                .color("#0000FF")
                .owner(testMember)
                .build();
        categoryRepository.save(newCategory);

        // 통계 다시 조회
        Statistics updatedStats = statisticsService.getStatistics(testMember.getId());

        // Then
        assertThat(updatedStats.getCategoryCount()).isEqualTo(3); // 카테고리 수 업데이트
        assertThat(updatedStats.getSucceededTodosCount()).isEqualTo(5); // 할일 수는 동일
    }

    @Test
    @DisplayName("할일 완료 후 통계 업데이트")
    void testGetStatistics_AfterCompletingTodo() {
        // Given - 초기 통계 조회
        Statistics initialStats = statisticsService.getStatistics(testMember.getId());
        assertThat(initialStats.getSucceededTodosCount()).isEqualTo(5);

        // When - 미완료 할일 하나를 완료로 변경
        Todo incompleteTodo = todoRepository.findAll().stream()
                .filter(todo -> !todo.getComplete())
                .findFirst()
                .orElseThrow();
        
        // 새로운 완료된 할일 생성 (기존 엔티티는 불변이므로)
        Todo completedTodo = Todo.builder()
                .todoId(new TodoId(100L, 0L))
                .title("새로 완료된 할일")
                .complete(true)
                .active(true)
                .category(category1)
                .owner(testMember)
                .date(LocalDate.now())
                .tags(Set.of("newly_completed"))
                .build();
        todoRepository.save(completedTodo);

        // 통계 다시 조회
        Statistics updatedStats = statisticsService.getStatistics(testMember.getId());

        // Then
        assertThat(updatedStats.getSucceededTodosCount()).isEqualTo(6); // 완료된 할일 수 증가
        assertThat(updatedStats.getCategoryCount()).isEqualTo(2); // 카테고리 수는 동일
    }
}