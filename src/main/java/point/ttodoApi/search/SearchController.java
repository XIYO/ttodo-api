package point.ttodoApi.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.category.application.CategorySearchService;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.challenge.application.ChallengeSearchService;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.member.application.MemberSearchService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.todo.application.TodoSearchService;
import point.ttodoApi.todo.domain.Todo;

/**
 * 통합 검색 컨트롤러 - 동적 쿼리 시스템 활용
 */
@Tag(name = "Search", description = "통합 검색 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final TodoSearchService todoSearchService;
    private final MemberSearchService memberSearchService;
    private final CategorySearchService categorySearchService;
    private final ChallengeSearchService challengeSearchService;
    
    @Operation(summary = "Todo 검색", description = "다양한 조건으로 Todo를 검색합니다")
    @GetMapping("/todos")
    public ResponseEntity<Page<Todo>> searchTodos(
            @Parameter(description = "검색 조건") @ModelAttribute TodoSearchService.TodoSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Member member) {
        
        request.setOwnerId(member.getId());
        return ResponseEntity.ok(todoSearchService.searchTodos(request, pageable));
    }
    
    @Operation(summary = "Member 검색", description = "멤버를 검색합니다 (관리자 전용)")
    @GetMapping("/members")
    public ResponseEntity<Page<Member>> searchMembers(
            @Parameter(description = "검색 조건") @ModelAttribute MemberSearchService.MemberSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(memberSearchService.searchMembers(request, pageable));
    }
    
    @Operation(summary = "Category 검색", description = "카테고리를 검색합니다")
    @GetMapping("/categories")
    public ResponseEntity<Page<Category>> searchCategories(
            @Parameter(description = "검색 조건") @ModelAttribute CategorySearchService.CategorySearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "orderIndex", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Member member) {
        
        request.setOwnerId(member.getId());
        return ResponseEntity.ok(categorySearchService.searchCategories(request, pageable));
    }
    
    @Operation(summary = "Challenge 검색", description = "챌린지를 검색합니다")
    @GetMapping("/challenges")
    public ResponseEntity<Page<Challenge>> searchChallenges(
            @Parameter(description = "검색 조건") @ModelAttribute ChallengeSearchService.ChallengeSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(challengeSearchService.searchChallenges(request, pageable));
    }
    
    @Operation(summary = "오늘의 미완료 Todo", description = "오늘 날짜의 완료되지 않은 Todo를 조회합니다")
    @GetMapping("/todos/today-incomplete")
    public ResponseEntity<?> getTodayIncompleteTodos(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(todoSearchService.getTodayIncompleteTodos(member.getId()));
    }
    
    @Operation(summary = "비활성 회원 조회", description = "오랜 기간 로그인하지 않은 회원을 조회합니다 (관리자 전용)")
    @GetMapping("/members/inactive")
    public ResponseEntity<Page<Member>> getInactiveMembers(
            @Parameter(description = "비활성 기준 일수") @RequestParam(defaultValue = "90") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        
        return ResponseEntity.ok(memberSearchService.getInactiveMembers(days, pageable));
    }
    
    @Operation(summary = "공개 챌린지 조회", description = "현재 진행 중인 공개 챌린지를 조회합니다")
    @GetMapping("/challenges/public-ongoing")
    public ResponseEntity<Page<Challenge>> getPublicOngoingChallenges(
            @PageableDefault(size = 20, sort = "endDate", direction = Sort.Direction.ASC) Pageable pageable) {
        
        return ResponseEntity.ok(challengeSearchService.getPublicOngoingChallenges(pageable));
    }
}