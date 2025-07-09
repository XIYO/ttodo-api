package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.todo.application.TodoOriginalService;

import java.util.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TodoOriginalService todoOriginalService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@todoOriginalService.canAccessTags(authentication.principal.id)")
    @Operation(
            summary = "태그 목록 조회",
            description = "사용자가 사용한 모든 태그 목록을 중복 없이 조회합니다. 페이지네이션을 지원합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "태그 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "응답 예시",
                                    value = """
                      {
                        "content": ["영어", "학습", "운동", "등산", "광교산", "손톱", "말랑"],
                        "pageable": {
                          "pageNumber": 0,
                          "pageSize": 10,
                          "sort": {
                            "empty": false,
                            "sorted": true,
                            "unsorted": false
                          }
                        },
                        "totalElements": 7,
                        "totalPages": 1,
                        "first": true,
                        "last": true,
                        "size": 10,
                        "numberOfElements": 7,
                        "empty": false
                      }
                      """
                            )
                    )
            )
    )
    public Page<String> getTags(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(required = false)
            @Parameter(description = "카테고리 ID 목록 (중복 허용)", example = "550e8400-e29b-41d4-a716-446655440000,550e8400-e29b-41d4-a716-446655440001")
            List<UUID> categoryIds,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "페이지 크기", example = "10")
            int size,
            @RequestParam(defaultValue = "asc")
            @Parameter(description = "정렬 방향 (asc: 오름차순, desc: 내림차순)", example = "asc")
            String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? 
            Sort.by("tag").descending() : Sort.by("tag").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return todoOriginalService.getTags(principal.id(), categoryIds, pageable);
    }
}
