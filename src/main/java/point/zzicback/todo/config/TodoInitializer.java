package point.zzicback.todo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

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

    List<Todo> defaultTodos = new ArrayList<>();

    // 기본 할일 추가
    defaultTodos.add(Todo.builder()
            .title("첫 번째 할일 완료하기")
            .description("이 할일을 완료 상태로 변경해보세요. 예: 오늘 할일을 체크해보세요.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("시작", "튜토리얼"))
            .member(member)
            .build());

    // === 개인 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("아침 명상하기")
            .description("하루를 시작하기 전 15분 명상으로 마음 정리하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("명상", "습관", "아침"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("일기 작성하기")
            .description("오늘 하루 있었던 일과 감정 정리하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("일기", "성찰", "습관"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("주간 목표 설정하기")
            .description("이번 주 성취할 3가지 목표 구체적으로 작성하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("목표", "계획", "성장"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("책 '원씽' 읽기")
            .description("오늘 30페이지 읽고 중요 내용 메모하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("독서", "성장", "지식"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("영화관 예매하기")
            .description("주말 개봉하는 '인터스텔라 2' 예매하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("취미", "영화", "여가"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("미용실 예약하기")
            .description("다음 주 수요일 오후 2시, 김스타일 미용실")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("미용", "예약", "관리"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("방 청소하기")
            .description("옷장 정리 및 책상 먼지 제거하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("청소", "정리", "집"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("비타민 구매하기")
            .description("종합비타민과 오메가3 온라인으로 주문하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("건강", "쇼핑", "영양제"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("새 신발 구매하기")
            .description("운동화 사이즈 270, 블랙 또는 네이비 색상")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("쇼핑", "패션", "신발"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("커피 사러가기")
            .description("원두 250g 구매, 에티오피아 예가체프 선호")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("커피", "쇼핑", "취미"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("블로그 글 작성하기")
            .description("여행 후기 및 사진 정리해서 올리기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("블로그", "글쓰기", "취미"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("식물 물주기")
            .description("거실과 서재의 식물에 물주기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("식물", "가꾸기", "집"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("독서모임 자료 준비")
            .description("이번 주 토론 주제 \"인공지능과 미래\" 발표 자료 준비")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("독서", "모임", "발표"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("명함 디자인 검토하기")
            .description("디자이너가 보낸 명함 시안 3개 검토 후 피드백 주기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("디자인", "명함", "피드백"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("여름 휴가 계획 세우기")
            .description("8월 첫째 주 제주도 3박4일 여행 일정 계획하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("개인", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("여행", "계획", "휴가"))
            .member(member)
            .build());

    // === 업무 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("업무 회의 준비")
            .description("업무 카테고리에 회의 준비 자료를 정리해보세요. 예: 회의 안건 정리, 자료 출력 등")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("회의", "업무", "중요"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("주간 업무 보고서 작성")
            .description("지난 주 성과와 다음 주 계획을 포함한 보고서 작성하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("보고서", "업무", "마감"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("프로젝트 일정 검토")
            .description("7월 출시 예정인 프로젝트 진행 상황 점검 및 일정 조정")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("프로젝트", "일정", "관리"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("팀 미팅 준비")
            .description("부서 간 협업 방안에 대한 안건 준비하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("미팅", "팀워크", "협업"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("이메일 정리하기")
            .description("미처리 이메일 답장 및 중요 메일 폴더 정리하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("이메일", "정리", "커뮤니케이션"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("신규 직원 교육 자료 준비")
            .description("온보딩 프로세스 및 업무 매뉴얼 업데이트하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("교육", "인사", "매뉴얼"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("거래처 미팅 준비")
            .description("A사 담당자와의 계약 갱신 관련 미팅 자료 준비")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("거래처", "계약", "미팅"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("분기별 실적 분석")
            .description("2/4분기 판매 실적 분석 및 보고서 작성하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("분석", "실적", "보고서"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("경비 정산하기")
            .description("지난 출장 경비 내역 정리 및 영수증 제출하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("경비", "정산", "출장"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("고객 피드백 검토하기")
            .description("최근 접수된 고객 피드백 분석 및 개선안 도출하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("고객", "피드백", "개선"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("웹사이트 개선 기획")
            .description("사용성 향상을 위한 웹사이트 UI/UX 개선점 정리하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("웹사이트", "UI", "기획"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("업무 매뉴얼 업데이트")
            .description("신규 시스템 도입에 따른 업무 프로세스 매뉴얼 갱신하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("매뉴얼", "프로세스", "문서"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("마케팅 전략 회의")
            .description("3분기 마케팅 캠페인 전략 수립 회의 참석하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("마케팅", "전략", "회의"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("인사평가 자료 준비")
            .description("팀원 반기 인사평가를 위한 실적 자료 준비하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("인사", "평가", "관리"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("API 문서 검토")
            .description("신규 개발된 API 문서 검토 및 피드백 제공하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("개발", "API", "문서"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("리스크 관리 보고서 작성")
            .description("프로젝트 잠재 리스크 분석 및 대응 방안 문서화하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("업무", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("리스크", "관리", "프로젝트"))
            .member(member)
            .build());

    // === 공부 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("영어 단어 암기")
            .description("하루에 영어 단어 10개 외우기")
            .priorityId(0)
            .dueDate(LocalDate.now().plusDays(7))
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .tags(Set.of("영어", "학습", "반복"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("알고리즘 문제 풀기")
            .description("프로그래머스 Level 2 문제 3개 풀기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("알고리즘", "코딩", "문제해결"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("스프링 부트 강의 듣기")
            .description("인프런 김영한 강사 강의 Section 4 완료하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("개발", "스프링", "강의"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("데이터 분석 과제 작성")
            .description("판매 데이터 분석 및 인사이트 도출 보고서 작성하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("데이터", "분석", "과제"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("일본어 문법 복습")
            .description("JLPT N3 문법 중 조건문 표현 복습하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("일본어", "문법", "복습"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("디자인 패턴 스터디")
            .description("팩토리 패턴과 옵저버 패턴 개념 학습하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("개발", "디자인패턴", "스터디"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("통계학 기초 복습")
            .description("확률분포와 가설검정 개념 복습 및 문제 풀이")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("통계", "수학", "복습"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("클라우드 컴퓨팅 개념 학습")
            .description("AWS 기초 서비스 개념 및 사용법 숙지하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("AWS", "클라우드", "IT"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("마케팅 전략 책 읽기")
            .description("'퍼스웨이전' 4장까지 읽고 핵심 개념 정리하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("마케팅", "독서", "전략"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("재테크 강의 듣기")
            .description("부동산 투자 기초 강의 2개 수강하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("재테크", "투자", "강의"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("포토샵 실습하기")
            .description("이미지 합성 및 색상 보정 기법 연습하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("디자인", "포토샵", "실습"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("SQL 문제 풀이")
            .description("복잡한 조인과 서브쿼리 문제 5개 풀기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("데이터베이스", "SQL", "개발"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("스페인어 회화 연습")
            .description("언어 교환 앱에서 30분 스페인어로 대화하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("스페인어", "회화", "언어"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("심리학 논문 읽기")
            .description("행동경제학 관련 논문 1편 읽고 요약하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("심리학", "논문", "연구"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("프로젝트 기획서 작성법 학습")
            .description("효과적인 프로젝트 기획서 작성 방법 익히기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("공부", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("기획", "문서", "프로젝트"))
            .member(member)
            .build());

    // === 가족 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("가족 일정 추가하기")
            .description("저녁 6시에 가족들과 식사.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("가족", "식사", "저녁"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("부모님 선물 구매하기")
            .description("어버이날 선물로 건강식품 구매하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("선물", "부모님", "효도"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("가족 여행 계획하기")
            .description("여름휴가 제주도 여행 일정 및 숙소 예약하기")
            .priorityId(2)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("여행", "가족", "계획"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("아이 병원 예약하기")
            .description("예방접종 일정 예약하기, 소아과")
            .priorityId(2)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("병원", "아이", "건강"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("주말 가족 모임 준비")
            .description("외가 친척들 방문, 간식 및 식사 준비하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("모임", "친척", "준비"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("자녀 학교 상담 참석")
            .description("담임 선생님과 학업 및 진로 상담, 오후 4시")
            .priorityId(2)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("상담", "학교", "교육"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("가족 앨범 정리하기")
            .description("지난달 여행 사진 인화 및 앨범 정리하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("사진", "추억", "정리"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("아이 학원 등록하기")
            .description("영어 학원 상담 및 등록 절차 진행하기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("교육", "학원", "아이"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("명절 준비 계획하기")
            .description("차례 음식 준비 및 선물 구매 계획 세우기")
            .priorityId(1)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("명절", "준비", "계획"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("가족 저녁 메뉴 정하기")
            .description("이번 주 저녁 식단 계획 및 장보기 목록 작성하기")
            .priorityId(0)
            .category(categoryMap.getOrDefault("가족", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("식단", "계획", "가족"))
            .member(member)
            .build());

    // === 약속 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("약속 관리하기")
            .description("친구들과 점심 약속.")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("친구", "점심", "사교"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("동창회 참석하기")
            .description("고등학교 10주년 동창회, 강남역 모임장소")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("동창회", "친구", "모임"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("업무 미팅 확정하기")
            .description("협력사 담당자와 오후 2시 카페에서 미팅")
            .priorityId(2)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("업무", "미팅", "네트워킹"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("멘토링 세션 참석")
            .description("경력 개발 관련 멘토와의 온라인 미팅")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("멘토링", "경력", "성장"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("친구 생일 파티")
            .description("저녁 7시, 홍대 레스토랑에서 생일 축하 모임")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("생일", "축하", "친구"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("스터디 그룹 모임")
            .description("프로그래밍 스터디, 강남역 스터디카페")
            .priorityId(2)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("스터디", "개발", "학습"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("취미 모임 참석")
            .description("사진 동호회 정기 모임 및 출사")
            .priorityId(0)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("취미", "사진", "모임"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("병원 예약 확인")
            .description("건강검진 예약, 오전 10시 종합병원")
            .priorityId(2)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("병원", "건강", "검진"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("부동산 방문")
            .description("신규 아파트 분양 상담, 오후 3시")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("부동산", "상담", "주택"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("온라인 컨퍼런스 참석")
            .description("개발자 컨퍼런스 줌 미팅, 저녁 8시")
            .priorityId(1)
            .category(categoryMap.getOrDefault("약속", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("컨퍼런스", "개발", "온라인"))
            .member(member)
            .build());

    // === 운동 카테고리 할일 ===
    defaultTodos.add(Todo.builder()
            .title("운동 계획 세우기")
            .description("복근 운동 3세트, 유산소 20분")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("운동", "건강", "복근"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("헬스장 PT 예약")
            .description("오후 7시, 상체 중점 트레이닝")
            .priorityId(1)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("PT", "헬스", "예약"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("러닝화 구매하기")
            .description("조깅용 신발 구매, 스포츠 브랜드 매장 방문")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("쇼핑", "운동용품", "러닝"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("필라테스 수업")
            .description("오전 10시, 코어 강화 클래스")
            .priorityId(1)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("필라테스", "코어", "건강"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("홈 트레이닝")
            .description("HIIT 15분, 스쿼트 20개 3세트")
            .priorityId(1)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(2))
            .tags(Set.of("홈트", "HIIT", "스쿼트"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("등산 계획하기")
            .description("주말 북한산 등산, 친구들과 오전 8시 출발")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("등산", "아웃도어", "취미"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("수영 강습 참석")
            .description("오후 6시, 자유형 기술 연습")
            .priorityId(1)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("수영", "강습", "기술"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("요가 클래스 참석")
            .description("오전 8시, 명상 중심 요가 수업")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("요가", "명상", "스트레칭"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("식단 계획 작성")
            .description("운동 효과 극대화를 위한 일주일 식단 계획")
            .priorityId(1)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(3))
            .tags(Set.of("식단", "영양", "건강"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("테니스 레슨")
            .description("포핸드 스트로크 연습, 오후 2시")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(2))
            .tags(Set.of("테니스", "레슨", "기술"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("스포츠 동호회 가입")
            .description("축구 동호회 가입 신청 및 첫 모임 참석")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(3))
            .tags(Set.of("축구", "동호회", "사교"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("체성분 측정")
            .description("헬스장 인바디 측정 및 목표 설정")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().minusDays(1))
            .tags(Set.of("인바디", "측정", "목표"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("사이클링 코스 계획")
            .description("한강 자전거 코스 40km 계획 및 장비 점검")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now())
            .tags(Set.of("사이클링", "자전거", "한강"))
            .member(member)
            .build());

    defaultTodos.add(Todo.builder()
            .title("스트레칭 루틴 연습")
            .description("전신 스트레칭 30분, 유연성 향상")
            .priorityId(0)
            .category(categoryMap.getOrDefault("운동", categories.isEmpty() ? null : categories.getFirst()))
            .dueDate(LocalDate.now().plusDays(1))
            .tags(Set.of("스트레칭", "유연성", "몸관리"))
            .member(member)
            .build());

    todoRepository.saveAll(defaultTodos);
    log.info("Created {} default todos for member {}", defaultTodos.size(), member.getNickname());
  }

  public void createDummyCategoriesForMember(Member member) {
    var dummyCategories = List.of(
        Category.builder().name("약속").color(null).description(null).member(member).build(),
        Category.builder().name("가족").color(null).description(null).member(member).build(),
        Category.builder().name("공부").color(null).description(null).member(member).build(),
        Category.builder().name("운동").color(null).description(null).member(member).build()
    );
    categoryRepository.saveAll(dummyCategories);
  }
}
