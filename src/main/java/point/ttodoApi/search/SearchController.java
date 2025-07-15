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
import point.ttodoApi.category.dto.request.CategorySearchRequest;
import point.ttodoApi.challenge.application.ChallengeSearchService;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.dto.request.ChallengeSearchRequest;
import point.ttodoApi.common.dto.PageResponse;
import point.ttodoApi.common.dto.SearchRequestUtils;
import point.ttodoApi.common.validation.ValidPageable;
import point.ttodoApi.common.validation.SortFieldsProvider;
import point.ttodoApi.member.application.MemberSearchService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.dto.request.MemberSearchRequest;
import point.ttodoApi.todo.application.TodoSearchService;
import point.ttodoApi.todo.domain.Todo;
import point.ttodoApi.todo.dto.request.TodoSearchRequest;

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
    private final SearchRequestUtils searchRequestUtils;
    
    @Operation(summary = "Todo 검색", description = "다양한 조건으로 Todo를 검색합니다")
    @GetMapping("/todos")
    @ValidPageable(sortFields = SortFieldsProvider.TODO)
    public ResponseEntity<PageResponse<Todo>> searchTodos(
            @Parameter(description = "검색 조건") @ModelAttribute TodoSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Member member) {
        
        request.setOwnerId(member.getId());
        request.validate();
        Page<Todo> result = todoSearchService.searchTodos(request, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    
    @Operation(summary = "Member 검색", description = "멤버를 검색합니다 (관리자 전용)")
    @GetMapping("/members")
    @ValidPageable(sortFields = SortFieldsProvider.MEMBER)
    public ResponseEntity<PageResponse<Member>> searchMembers(
            @Parameter(description = "검색 조건") @ModelAttribute MemberSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        request.validate();
        Page<Member> result = memberSearchService.searchMembers(request, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    
    @Operation(summary = "Category 검색", description = "카테고리를 검색합니다")
    @GetMapping("/categories")
    @ValidPageable(sortFields = SortFieldsProvider.CATEGORY)
    public ResponseEntity<PageResponse<Category>> searchCategories(
            @Parameter(description = "검색 조건") @ModelAttribute CategorySearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "orderIndex", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Member member) {
        
        request.setOwnerId(member.getId());
        request.validate();
        Page<Category> result = categorySearchService.searchCategories(request, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    
    @Operation(summary = "Challenge 검색", description = "챌린지를 검색합니다")
    @GetMapping("/challenges")
    @ValidPageable(sortFields = SortFieldsProvider.CHALLENGE)
    public ResponseEntity<PageResponse<Challenge>> searchChallenges(
            @Parameter(description = "검색 조건") @ModelAttribute ChallengeSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보") 
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        request.validate();
        Page<Challenge> result = challengeSearchService.searchChallenges(request, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    
    @Operation(summary = "오늘의 미완료 Todo", description = "오늘 날짜의 완료되지 않은 Todo를 조회합니다")
    @GetMapping("/todos/today-incomplete")
    public ResponseEntity<?> getTodayIncompleteTodos(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(todoSearchService.getTodayIncompleteTodos(member.getId()));
    }
    
    @Operation(summary = "비활성 회원 조회", description = "오랜 기간 로그인하지 않은 회원을 조회합니다 (관리자 전용)")
    @GetMapping("/members/inactive")
    @ValidPageable(sortFields = SortFieldsProvider.MEMBER)
    public ResponseEntity<PageResponse<Member>> getInactiveMembers(
            @Parameter(description = "비활성 기준 일수") @RequestParam(defaultValue = "90") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<Member> result = memberSearchService.getInactiveMembers(days, pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
    
    @Operation(summary = "공개 챌린지 조회", description = "현재 진행 중인 공개 챌린지를 조회합니다")
    @GetMapping("/challenges/public-ongoing")
    @ValidPageable(sortFields = SortFieldsProvider.CHALLENGE)
    public ResponseEntity<PageResponse<Challenge>> getPublicOngoingChallenges(
            @PageableDefault(size = 20, sort = "endDate", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<Challenge> result = challengeSearchService.getPublicOngoingChallenges(pageable);
        return ResponseEntity.ok(PageResponse.of(result));
    }
}